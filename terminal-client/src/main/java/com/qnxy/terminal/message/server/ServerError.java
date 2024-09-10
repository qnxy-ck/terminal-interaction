package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
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
    ;


    public static ServerError decode(ByteBuf buffer) {
        byte errCode = buffer.readByte();

        return Arrays.stream(values())
                .filter(it -> it.ordinal() == errCode)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知错误代码: " + errCode));
    }

    @Override
    public String toString() {
        return "ServerError." + name();
    }
}
