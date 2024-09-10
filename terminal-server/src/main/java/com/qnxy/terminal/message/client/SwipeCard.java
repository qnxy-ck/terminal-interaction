package com.qnxy.terminal.message.client;

import com.qnxy.terminal.IllegalEncodingException;
import com.qnxy.terminal.IllegalResponseMessageException;
import com.qnxy.terminal.ProactiveSyncMessageProcessor;
import com.qnxy.terminal.api.TerminalExternalService;
import com.qnxy.terminal.api.data.SwipeCardCallbackReq;
import com.qnxy.terminal.api.data.SwipeCardMethod;
import com.qnxy.terminal.api.data.SwipeCardResp;
import com.qnxy.terminal.client.ClientContext;
import com.qnxy.terminal.client.TerminalClient;
import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.server.ServerError;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static com.qnxy.terminal.api.data.SwipeCardCallbackReq.withErrorCode;
import static com.qnxy.terminal.api.data.SwipeCardCallbackReq.withSuccess;
import static com.qnxy.terminal.message.server.AuthorizedMoveOutGoods.withSwipeCardResp;

/**
 * 终端刷卡消息
 *
 * @param swipeCardMethod 刷卡方式
 * @param cardCode        卡的编码
 * @author Qnxy
 */
@Slf4j
public record SwipeCard(
        SwipeCardMethod swipeCardMethod,
        String cardCode
) implements ProactiveSyncMessageProcessor, ClientMessage {

    /**
     * 解码终端发送的消息为当前类型
     */
    public static SwipeCard decode(ByteBuf buffer) {
        final byte typeNumber = buffer.readByte();

        final SwipeCardMethod swipeCardMethod = SwipeCardMethod.typeNumberOf(typeNumber)
                .orElseThrow(() -> new IllegalEncodingException("未知刷卡类型: " + typeNumber));

        final String cardCode = buffer.readCharSequence(buffer.readableBytes(), StandardCharsets.UTF_8).toString();
        return new SwipeCard(swipeCardMethod, cardCode);
    }

    /**
     * 处理终端刷卡后的操作
     *
     * @param terminalClient      当前终端
     * @param responseMessageFlux 终端后续发送的信息
     */
    @Override
    public Mono<Void> handle(TerminalClient terminalClient, Flux<ClientMessage> responseMessageFlux) {
        return Mono.deferContextual(ctx -> Mono.just(ctx.get(ClientContext.class)))
                .flatMap(clientContext -> {
                            final TerminalExternalService terminalExternalService = clientContext.getTerminalExternalService();
                            final Long terminalId = clientContext.getTerminalId();

                            return terminalExternalService.swipeCard(terminalId, this.swipeCardMethod, this.cardCode)
                                    .onErrorResume(e -> swipeCardError(terminalId, terminalClient, e))
                                    .doOnNext(it -> terminalClient.send(withSwipeCardResp(it)))
                                    .flatMap(it -> this.respHandler(responseMessageFlux, it.transactionCode()))
                                    .flatMap(it -> this.swipeCardCallback(terminalExternalService, it));
                        }
                );
    }

    /**
     * 刷卡异常处理
     */
    private Mono<SwipeCardResp> swipeCardError(Long terminalId, TerminalClient terminalClient, Throwable e) {
        log.error("刷卡信息查询失败错误 terminalId: {} -- {}", terminalId, this, e);
        return Mono.fromRunnable(() -> terminalClient.send(ServerError.CARD_INFORMATION_QUERY_ERROR_FAILED));
    }


    /**
     * 处理终端后续消息
     * 在刷卡信息验证确认授权出货后的响应流程
     *
     * @param responseMessageFlux 响应信息
     * @param transactionCode     交易码
     * @return 刷卡出货后的回调信息
     */
    private Mono<SwipeCardCallbackReq> respHandler(Flux<ClientMessage> responseMessageFlux, long transactionCode) {
        return responseMessageFlux.next()
                .handle((it, sink) -> {
                    if (it instanceof AuthorizedMoveOutGoodsReceipt moveOutGoodsReceipt) {
                        sink.next(withSuccess(transactionCode, moveOutGoodsReceipt.tagsCode(), moveOutGoodsReceipt.alreadyTakenOut(), moveOutGoodsReceipt.tagsType()));
                        return;
                    }

                    if (it instanceof ErrorMessage errorMessage) {
                        sink.next(withErrorCode(transactionCode, errorMessage.errorCodes()));
                        return;
                    }

                    sink.error(new IllegalResponseMessageException());
                });
    }

    /**
     * 刷卡回调
     * 并进行重试
     */
    private Mono<Void> swipeCardCallback(TerminalExternalService terminalExternalService, SwipeCardCallbackReq swipeCardCallbackReq) {
        return terminalExternalService.swipeCardCallback(swipeCardCallbackReq)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(3)))
                .onErrorResume(e -> {
                    if (Exceptions.isRetryExhausted(e)) {
                        log.error("刷卡回调失败且重试后仍失败", e);
                    }
                    return Mono.error(e);
                })
                ;
    }


}
