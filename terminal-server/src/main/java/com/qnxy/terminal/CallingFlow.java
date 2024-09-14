package com.qnxy.terminal;

import com.qnxy.terminal.client.TerminalClient;
import com.qnxy.terminal.exceptions.TerminalExecuteException;
import com.qnxy.terminal.message.client.AuthorizedMoveOutGoodsReceipt;
import com.qnxy.terminal.message.client.ErrorMessage;
import com.qnxy.terminal.message.client.SetupSuccessful;
import com.qnxy.terminal.message.server.AuthorizedMoveOutGoods;
import com.qnxy.terminal.message.server.ServerError;
import com.qnxy.terminal.message.server.VolumeAdjustment;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * 终端机器调用流程
 *
 * @author Qnxy
 */
@Slf4j
public final class CallingFlow {

    public static Mono<AuthorizedMoveOutGoodsReceipt> authorizedMoveOutGoods(Long terminalId, AuthorizedMoveOutGoods message) {
        return Mono.defer(() -> {
            Optional<TerminalClient> clientOptional = ClientManager.findClient(terminalId);
            if (clientOptional.isEmpty()) {
                return Mono.error(new IllegalArgumentException("找不到客户端: " + terminalId));
            }

            TerminalClient client = clientOptional.get();
            return client.exchange(message)
                    .last()
                    .flatMap(it -> {
                        if (it instanceof AuthorizedMoveOutGoodsReceipt moveOutGoodsReceipt) {
                            return Mono.just(moveOutGoodsReceipt);
                        }

                        if (it instanceof ErrorMessage errorMessage) {
                            return Mono.error(new TerminalExecuteException(errorMessage.errorCodes()));
                        }

                        return flowStateError(client);
                    });

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
                    if (it instanceof SetupSuccessful) {
                        return Mono.just(true);
                    }

                    if (it instanceof ErrorMessage) {
                        log.debug("音量调节失败: {}", it);
                        return Mono.just(false);
                    }

                    return flowStateError(terminalClient);
                });
    }

    private static <T> Mono<T> flowStateError(TerminalClient terminalClient) {
        return terminalClient.close(ServerError.UNEXPECTED_MESSAGE_ERROR)
                .then(Mono.error(new IllegalStateException("终端执行流程返回结果非法, 该客户端已被关闭")));
    }

}
