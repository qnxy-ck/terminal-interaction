package com.qnxy.terminal.processor;

import com.qnxy.terminal.CallingFlow;
import com.qnxy.terminal.ClientMessageProcessor;
import com.qnxy.terminal.api.TerminalExternalService;
import com.qnxy.terminal.client.Client;
import com.qnxy.terminal.client.ClientContext;
import com.qnxy.terminal.message.ServerErrorMessage;
import com.qnxy.terminal.message.client.SwipeCard;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import static com.qnxy.terminal.message.server.AuthorizedMoveOutGoodsMessage.withSwipeCardResp;

/**
 * 刷卡处理
 *
 * @author Qnxy
 */
@Slf4j
public class SwipeCardMessageProcessor implements ClientMessageProcessor<SwipeCard> {

    @Override
    public Mono<Void> handle(SwipeCard swipeCard, Client client) {
        return Mono.deferContextual(ctx -> {
            final Long terminalId = ctx.get(ClientContext.class).getTerminalId();
            final TerminalExternalService terminalExternalService = ctx.get(TerminalExternalService.class);

            // 调用外部刷卡接口
            return terminalExternalService.swipeCard(terminalId, swipeCard.cardCode())
                    .onErrorResume(e -> {
                        log.error("刷卡信息查询失败错误 terminalId: {} -- {}", terminalId, swipeCard, e);
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
