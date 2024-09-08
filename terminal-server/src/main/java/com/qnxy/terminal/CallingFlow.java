package com.qnxy.terminal;

import com.qnxy.terminal.api.data.SwipeCardCallbackReq;
import com.qnxy.terminal.client.Client;
import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.ServerErrorMessage;
import com.qnxy.terminal.message.client.AdjustmentSuccessful;
import com.qnxy.terminal.message.client.AuthorizedMoveOutGoodsReceipt;
import com.qnxy.terminal.message.client.ErrorMessage;
import com.qnxy.terminal.message.server.AuthorizedMoveOutGoodsMessage;
import com.qnxy.terminal.message.server.VolumeAdjustment;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static com.qnxy.terminal.api.data.SwipeCardCallbackReq.withErrorCode;

/**
 * 终端机器调用流程
 *
 * @author Qnxy
 */
@Slf4j
public final class CallingFlow {

    /**
     * 授权机器出货
     *
     * @param client          那个终端
     * @param transactionCode 交易码
     * @param message         授权信息
     * @param callback        回调
     */
    public static Mono<Void> authorizedMoveOutGoods(
            Client client,
            long transactionCode,
            AuthorizedMoveOutGoodsMessage message,
            Function<SwipeCardCallbackReq, Mono<Void>> callback
    ) {

        return client.exchange(message)
                .last()
                .flatMap(it -> {
                    SwipeCardCallbackReq swipeCardCallbackReq;

                    if (it instanceof AuthorizedMoveOutGoodsReceipt receiptMessage) {
                        swipeCardCallbackReq = SwipeCardCallbackReq.withSuccess(
                                transactionCode,
                                receiptMessage.tagsCode(),
                                receiptMessage.alreadyTakenOut(),
                                receiptMessage.tagsType()
                        );
                    } else if (it instanceof ErrorMessage errorMessage) {
                        swipeCardCallbackReq = withErrorCode(transactionCode, errorMessage.errorCodes());
                    } else {
                        client.send(ServerErrorMessage.UNEXPECTED_MESSAGE_ERROR);
                        return client.close();
                    }

                    return callback.apply(swipeCardCallbackReq);
                });
    }

    /**
     * 同步授权出货接口
     */
    public static Mono<AuthorizedMoveOutGoodsReceipt> authorizedMoveOutGoodsSync(Client client, AuthorizedMoveOutGoodsMessage message) {
        return client.exchange(message)
                .last()
                .flatMap(it -> expectMessage(
                        it,
                        AuthorizedMoveOutGoodsReceipt.class,
                        client,
                        Function.identity(),
                        errMsg -> Mono.error(new TerminalExecuteException(errMsg.errorCodes()))
                ));
    }

    /**
     * 音量调节
     *
     * @param client 被调节的机器
     * @param volume 调节的音量
     * @return 成功返回true
     */
    public static Mono<Boolean> volumeAdjustment(Client client, byte volume) {
        return client.exchange(new VolumeAdjustment(volume))
                .last()
                .flatMap(it -> expectMessage(
                        it,
                        AdjustmentSuccessful.class,
                        client,
                        expectMsg -> true,
                        err -> Mono.just(false)
                ));
    }


    private static <EXPECT_MESSAGE extends ClientMessage, T> Mono<T> expectMessage(
            ClientMessage clientMessage,
            Class<EXPECT_MESSAGE> expectedMessageClass,
            Client client,

            Function<EXPECT_MESSAGE, T> expectFunction,
            Function<ErrorMessage, Mono<T>> errorFunction
    ) {
        if (clientMessage instanceof ErrorMessage errorMessage) {
            return errorFunction.apply(errorMessage);
        }

        if (expectedMessageClass.isAssignableFrom(clientMessage.getClass())) {
            return Mono.just(expectFunction.apply(expectedMessageClass.cast(clientMessage)));
        }

        client.send(ServerErrorMessage.UNEXPECTED_MESSAGE_ERROR);
        return client.close().then(Mono.empty());
    }


}
