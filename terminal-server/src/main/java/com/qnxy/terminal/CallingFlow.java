package com.qnxy.terminal;

import com.qnxy.terminal.api.data.SwipeCardCallbackReq;
import com.qnxy.terminal.client.TerminalClient;
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
     * @param terminalClient          那个终端
     * @param transactionCode 交易码
     * @param message         授权信息
     * @param callback        回调
     */
    public static Mono<Void> authorizedMoveOutGoods(
            TerminalClient terminalClient,
            long transactionCode,
            AuthorizedMoveOutGoodsMessage message,
            Function<SwipeCardCallbackReq, Mono<Void>> callback
    ) {

        return terminalClient.exchange(message)
                .last()
                .flatMap(it -> expectMessage(
                        it,
                        AuthorizedMoveOutGoodsReceipt.class,
                        terminalClient,
                        expectMessage -> SwipeCardCallbackReq.withSuccess(
                                transactionCode,
                                expectMessage.tagsCode(),
                                expectMessage.alreadyTakenOut(),
                                expectMessage.tagsType()
                        ),
                        errorMessage -> Mono.just(withErrorCode(transactionCode, errorMessage.errorCodes()))
                ))
                .flatMap(callback);
    }

    /**
     * 同步授权出货接口
     */
    public static Mono<AuthorizedMoveOutGoodsReceipt> authorizedMoveOutGoodsSync(TerminalClient terminalClient, AuthorizedMoveOutGoodsMessage message) {
        return terminalClient.exchange(message)
                .last()
                .flatMap(it -> expectMessage(
                        it,
                        AuthorizedMoveOutGoodsReceipt.class,
                        terminalClient,
                        Function.identity(),
                        errMsg -> Mono.error(new TerminalExecuteException(errMsg.errorCodes()))
                ));
    }

    /**
     * 音量调节
     *
     * @param terminalClient 被调节的机器
     * @param volume 调节的音量
     * @return 成功返回true
     */
    public static Mono<Boolean> volumeAdjustment(TerminalClient terminalClient, byte volume) {
        return terminalClient.exchange(new VolumeAdjustment(volume))
                .last()
                .flatMap(it -> expectMessage(
                        it,
                        AdjustmentSuccessful.class,
                        terminalClient,
                        expectMsg -> true,
                        err -> Mono.just(false)
                ));
    }


    private static <EXPECT_MESSAGE extends ClientMessage, T> Mono<T> expectMessage(
            ClientMessage clientMessage,
            Class<EXPECT_MESSAGE> expectedMessageClass,
            TerminalClient terminalClient,
            Function<EXPECT_MESSAGE, T> expectFunction,
            Function<ErrorMessage, Mono<T>> errorFunction
    ) {
        if (clientMessage instanceof ErrorMessage errorMessage) {
            return errorFunction.apply(errorMessage);
        }

        if (expectedMessageClass.isAssignableFrom(clientMessage.getClass())) {
            return Mono.just(expectFunction.apply(expectedMessageClass.cast(clientMessage)));
        }

        terminalClient.send(ServerErrorMessage.UNEXPECTED_MESSAGE_ERROR);
        return terminalClient.close().then(Mono.empty());
    }


}
