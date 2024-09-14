package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 服务端发出的各种错误类型
 *
 * @author Qnxy
 */
@RequiredArgsConstructor
public enum ServerError implements ServerMessage {

    /**
     * 等待授权已达最大时间错误
     */
    MAXIMUM_AUTHORIZATION_WAIT_ERROR,

    /**
     * 拒绝连接
     */
    CONNECTION_REFUSED_ERROR,

    /**
     * 意外的消息
     */
    UNEXPECTED_MESSAGE_ERROR,

    /**
     * 刷卡信息查询失败错误
     */
    CARD_INFORMATION_QUERY_ERROR_FAILED,

    /**
     * 心跳超时
     */
    HEARTBEAT_TIMEOUT_ERROR,

    /**
     * 同步任务进行中
     */
    SYNCHRONIZATION_TASK_IN_PROGRESS,

    /**
     * 服务器异常
     */
    SERVER_EXCEPTION,

    ;

    public static ServerMessage decode(ByteBuf buf) {
        byte b = buf.readByte();

        return Arrays.stream(values())
                .filter(e -> e.ordinal() == b)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unknown Server Error"));
    }

    @Override
    public String toString() {
        return "ServerError." + this.name();
    }
}
