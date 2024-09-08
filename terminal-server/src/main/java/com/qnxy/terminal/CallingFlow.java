package com.qnxy.terminal;

import com.qnxy.terminal.api.AuthorizedMoveOutGoodsService;
import com.qnxy.terminal.api.data.SwipeCardCallbackReq;
import com.qnxy.terminal.client.Client;
import com.qnxy.terminal.message.ServerErrorMessage;
import com.qnxy.terminal.message.client.AuthorizedMoveOutGoodsReceiptMessage;
import com.qnxy.terminal.message.client.ErrorMessage;
import com.qnxy.terminal.message.server.AuthorizedMoveOutGoodsMessage;
import reactor.core.publisher.Mono;

/**
 * 终端机器调用流程
 * 
 * @author Qnxy
 */
public final class CallingFlow {

    /**
     * 授权机器出货
     *
     * @param client                        那个终端
     * @param transactionCode               交易码
     * @param message                       授权信息
     * @param authorizedMoveOutGoodsService 外部服务
     */
    public static Mono<Void> authorizedMoveOutGoods(
            Client client,
            long transactionCode,
            AuthorizedMoveOutGoodsMessage message,
            AuthorizedMoveOutGoodsService authorizedMoveOutGoodsService
    ) {
        
        return client.exchange(message)
                .last()
                .flatMap(it -> {
                    SwipeCardCallbackReq swipeCardCallbackReq;

                    if (it instanceof AuthorizedMoveOutGoodsReceiptMessage receiptMessage) {
                        swipeCardCallbackReq = SwipeCardCallbackReq.withSuccess(
                                transactionCode,
                                receiptMessage.tagsCode(),
                                receiptMessage.alreadyTakenOut(),
                                receiptMessage.tagsType()
                        );
                    } else if (it instanceof ErrorMessage errorMessage) {
                        swipeCardCallbackReq = SwipeCardCallbackReq.withErrorCode(transactionCode, errorMessage.errorCodes());
                    } else {
                        client.send(ServerErrorMessage.UNEXPECTED_MESSAGE_ERROR);
                        return client.close();
                    }

                    return authorizedMoveOutGoodsService.swipeCardCallback(swipeCardCallbackReq);
                });

    }
}
