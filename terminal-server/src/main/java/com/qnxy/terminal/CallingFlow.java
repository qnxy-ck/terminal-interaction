package com.qnxy.terminal;

import com.qnxy.terminal.api.data.SwipeCardCallbackReq;
import com.qnxy.terminal.client.TerminalClient;
import com.qnxy.terminal.message.client.AdjustmentSuccessful;
import com.qnxy.terminal.message.client.AuthorizedMoveOutGoodsReceipt;
import com.qnxy.terminal.message.client.ErrorMessage;
import com.qnxy.terminal.message.server.AuthorizedMoveOutGoods;
import com.qnxy.terminal.message.server.ServerError;
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
     * @param terminalClient  那个终端
     * @param transactionCode 交易码
     * @param message         授权信息
     * @param callback        回调
     */
    public static Mono<Void> authorizedMoveOutGoods(TerminalClient terminalClient, long transactionCode, AuthorizedMoveOutGoods message, Function<SwipeCardCallbackReq, Mono<Void>> callback) {

        return terminalClient.exchange(message)
                .last()
                .flatMap(it -> {
                    if (it instanceof AuthorizedMoveOutGoodsReceipt moveOutGoodsReceipt) {
                        return Mono.just(SwipeCardCallbackReq.withSuccess(
                                transactionCode,
                                moveOutGoodsReceipt.tagsCode(),
                                moveOutGoodsReceipt.alreadyTakenOut(),
                                moveOutGoodsReceipt.tagsType()
                        ));
                    }

                    if (it instanceof ErrorMessage errorMessage) {
                        return Mono.just(withErrorCode(transactionCode, errorMessage.errorCodes()));
                    }


                    return terminalClient.close(ServerError.UNEXPECTED_MESSAGE_ERROR).then(Mono.empty());
                })
                .flatMap(callback);
    }

    /**
     * 同步授权出货接口
     */
    public static Mono<AuthorizedMoveOutGoodsReceipt> authorizedMoveOutGoodsSync(TerminalClient terminalClient, AuthorizedMoveOutGoods message) {
        return terminalClient.exchange(message)
                .last()
                .flatMap(it -> {
                    if (it instanceof AuthorizedMoveOutGoodsReceipt moveOutGoodsReceipt) {
                        return Mono.just(moveOutGoodsReceipt);
                    }

                    if (it instanceof ErrorMessage errorMessage) {
                        return Mono.error(new TerminalExecuteException(errorMessage.errorCodes()));
                    }

                    return terminalClient.close(ServerError.UNEXPECTED_MESSAGE_ERROR).then(Mono.empty());
                });
    }

    /**
     * 音量调节
     *
     * @param terminalClient 被调节的机器
     * @param volume         调节的音量
     * @return 成功返回true
     */
    public static Mono<Boolean> volumeAdjustment(TerminalClient terminalClient, byte volume) {
        return terminalClient.exchange(new VolumeAdjustment(volume))
                .last()
                .flatMap(it -> {
                    if (it instanceof AdjustmentSuccessful) {
                        return Mono.just(true);
                    }

                    if (it instanceof ErrorMessage) {
                        log.debug("音量调节失败: {}", it);
                        return Mono.just(false);
                    }

                    return terminalClient.close(ServerError.UNEXPECTED_MESSAGE_ERROR).then(Mono.empty());
                });
    }

}
