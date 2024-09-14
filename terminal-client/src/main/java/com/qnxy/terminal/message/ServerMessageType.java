package com.qnxy.terminal.message;

import com.qnxy.terminal.message.server.ServerError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * 服务端发出的消息类型
 *
 * @author Qnxy
 */
@RequiredArgsConstructor
@Getter
public enum ServerMessageType {

    /**
     * 授权出货
     */
    AUTHORIZED_MOVE_OUT_GOODS('F'),

    /**
     * 授权申请通过
     */
    AUTHORIZATION_APPLICATION_PASSED('s'),

    /**
     * 最后的完成响应
     * 如心跳的响应等
     */
    COMPLETE('c'),

    /**
     * 终端机器音量调节
     */
    VOLUME_ADJUSTMENT('V'),

    /**
     * 服务端发出的各种错误 {@link ServerError}
     */
    SERVER_ERROR('E'),
    ;

    /**
     * 消息类型对应的指令码
     */
    private final char instructionCode;

    public static Optional<ServerMessageType> fromInstructionCode(final char instructionCode) {
        return Arrays.stream(values())
                .filter(it -> it.instructionCode == instructionCode)
                .findFirst();
    }


}
