package com.qnxy.terminal.processor;

import com.qnxy.terminal.ProactiveSyncMessageProcessor;
import com.qnxy.terminal.ServerConfiguration;
import com.qnxy.terminal.api.TerminalExternalService;
import com.qnxy.terminal.api.data.ErrorCode;
import com.qnxy.terminal.api.data.SwipeCardCallbackReq;
import com.qnxy.terminal.api.data.SwipeCardMethod;
import com.qnxy.terminal.api.data.SwipeCardResp;
import com.qnxy.terminal.client.ClientContext;
import com.qnxy.terminal.client.TerminalClient;
import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.client.AuthorizedMoveOutGoodsReceipt;
import com.qnxy.terminal.message.client.ErrorMessage;
import com.qnxy.terminal.message.client.SwipeCard;
import com.qnxy.terminal.message.server.AuthorizedMoveOutGoods;
import com.qnxy.terminal.message.server.ServerError;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import static com.qnxy.terminal.api.data.ErrorCode.COMMUNICATION_FAILURE;
import static com.qnxy.terminal.api.data.SwipeCardCallbackReq.withErrorCode;
import static com.qnxy.terminal.api.data.SwipeCardCallbackReq.withSuccess;
import static com.qnxy.terminal.message.server.AuthorizedMoveOutGoods.withSwipeCardResp;

/**
 * 用户刷卡 同步处理器
 * <p>
 * 执行流程:
 * <p>
 * 1. 收到消息后调用外部服务 {@link TerminalExternalService#swipeCard(long, SwipeCardMethod, String)}
 * 如果调用失败将返回 {@link ServerError#CARD_INFORMATION_QUERY_ERROR_FAILED}
 * 成功返回 {@link AuthorizedMoveOutGoods}
 * <p>
 * 2. 发送 {@link AuthorizedMoveOutGoods} 后等待终端回执消息
 * <p>
 * 期待返回 {@link AuthorizedMoveOutGoodsReceipt} {@link ErrorMessage} 消息
 * 否则发送消息 {@link ServerError#UNEXPECTED_MESSAGE_ERROR} 并且关闭连接
 * <p>
 * 3. 根据返回的消息调用 {@link TerminalExternalService#swipeCardCallback(SwipeCardCallbackReq)} 接口且失败根据配置重试
 *
 * @author Qnxy
 */
@Slf4j
public class SwipeCardProactiveSyncMessageProcessor implements ProactiveSyncMessageProcessor<SwipeCard> {

    @Override
    public Mono<Void> handle(TerminalClient client, SwipeCard swipeCard, Flux<ClientMessage> responseMessageFlux) {
        return Mono.deferContextual(ctx -> {
            final ClientContext clientContext = ctx.get(ClientContext.class);
            final Long terminalId = clientContext.getTerminalId();
            final TerminalExternalService terminalExternalService = clientContext.getServerContext().terminalExternalService();

            return terminalExternalService.swipeCard(terminalId, swipeCard.swipeCardMethod(), swipeCard.cardCode())
                    .onErrorResume(e -> swipeCardError(terminalId, client, e, swipeCard))
                    .doOnNext(it -> client.send(withSwipeCardResp(it)))
                    .flatMap(it -> this.respHandler(responseMessageFlux, it.transactionCode(), client, clientContext))
                    .flatMap(it -> this.swipeCardCallback(terminalExternalService, it, clientContext));
        });
    }


    /**
     * 刷卡服务调用异常处理
     */
    private Mono<SwipeCardResp> swipeCardError(Long terminalId, TerminalClient terminalClient, Throwable e, SwipeCard swipeCard) {
        log.error("终端: {} - 刷卡信息查询失败错误: {}", terminalId, swipeCard, e);
        terminalClient.send(ServerError.CARD_INFORMATION_QUERY_ERROR_FAILED);
        return Mono.empty();
    }


    /**
     * 处理终端后续消息
     * 在刷卡信息验证确认授权出货后的响应流程
     *
     * @param responseMessageFlux 响应信息
     * @param transactionCode     交易码
     * @param clientContext
     * @return 刷卡出货后的回调信息
     */
    private Mono<SwipeCardCallbackReq> respHandler(Flux<ClientMessage> responseMessageFlux, long transactionCode, TerminalClient client, ClientContext clientContext) {
        return responseMessageFlux.next()
                .flatMap(it -> {
                    if (it instanceof AuthorizedMoveOutGoodsReceipt receipt) {
                        return Mono.just(withSuccess(transactionCode, receipt.tagsCode(), receipt.alreadyTakenOut()));
                    }

                    if (it instanceof ErrorMessage errorMessage) {
                        return Mono.just(withErrorCode(transactionCode, errorMessage.errorCodes()));
                    }

                    log.error("终端: {} - 收到意外的终端消息, 连接将被关闭. 消息内容: {}", clientContext.getTerminalId(), it);
                    return client.close(ServerError.UNEXPECTED_MESSAGE_ERROR)
                            .thenReturn(withErrorCode(transactionCode, new ErrorCode[]{COMMUNICATION_FAILURE}));
                })
                ;
    }

    /**
     * 刷卡回调
     * 并进行重试
     */
    private Mono<Void> swipeCardCallback(TerminalExternalService terminalExternalService, SwipeCardCallbackReq swipeCardCallbackReq, ClientContext clientContext) {
        final ServerConfiguration configuration = clientContext.getServerContext().serverConfiguration();

        return terminalExternalService.swipeCardCallback(swipeCardCallbackReq)
                .retryWhen(Retry.backoff(
                        configuration.swipeCardAuthMoveOutGoodsCallbackRetryCount(),
                        configuration.swipeCardAuthMoveOutGoodsCallbackRetryBackoff()
                ))
                .onErrorResume(e -> {
                    if (Exceptions.isRetryExhausted(e)) {
                        log.error("终端: {} - 刷卡回调失败且重试后仍失败, 回调消息: {}", clientContext.getTerminalId(), swipeCardCallbackReq, e);
                    }
                    return Mono.error(e);
                })
                ;
    }

}
