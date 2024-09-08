package com.qnxy.terminal.message.client;

import com.qnxy.terminal.CallingFlow;
import com.qnxy.terminal.api.TerminalExternalService;
import com.qnxy.terminal.api.data.SwipeCardMethod;
import com.qnxy.terminal.client.Client;
import com.qnxy.terminal.client.ClientContext;
import com.qnxy.terminal.message.ServerErrorMessage;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static com.qnxy.terminal.message.server.AuthorizedMoveOutGoodsMessage.withSwipeCardResp;

/**
 * @author Qnxy
 */
@Slf4j
public record SwipeCard(
        SwipeCardMethod swipeCardMethod,
        String cardCode
) implements ProactiveMessages {

    public static SwipeCard decode(ByteBuf buffer) {
        final byte typeNumber = buffer.readByte();

        final SwipeCardMethod swipeCardMethod = SwipeCardMethod.typeNumberOf(typeNumber)
                .orElseThrow(() -> new IllegalArgumentException("Unknown SwipeCard type: " + typeNumber));

        final String cardCode = buffer.readCharSequence(buffer.readerIndex(), StandardCharsets.UTF_8)
                .toString();

        return new SwipeCard(swipeCardMethod, cardCode);
    }

    @Override
    public Mono<Void> handle(Client client) {
        return Mono.deferContextual(ctx -> {
            final Long terminalId = ctx.get(ClientContext.class).getTerminalId();
            final TerminalExternalService terminalExternalService = ctx.get(TerminalExternalService.class);

            // 调用外部刷卡接口
            return terminalExternalService.swipeCard(terminalId, this.cardCode)
                    .onErrorResume(e -> {
                        log.error("刷卡信息查询失败错误 terminalId: {} -- {}", terminalId, this, e);
                        client.send(ServerErrorMessage.CARD_INFORMATION_QUERY_ERROR_FAILED);
                        return Mono.empty();
                    })
                    .flatMap(it -> CallingFlow.authorizedMoveOutGoods(
                            client,
                            it.transactionCode(),
                            withSwipeCardResp(it),
                            terminalExternalService::swipeCardCallback
                    ));
        });
    }
}
