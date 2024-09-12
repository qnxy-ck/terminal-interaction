package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import com.qnxy.terminal.message.ServerMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.RequiredArgsConstructor;

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


    @Override
    public ByteBuf encode(ByteBufAllocator byteBufAllocator) {
        return this.simpleByteBuf(
                byteBufAllocator,
                ServerMessageType.SERVER_ERROR,
                byteBuf -> byteBuf.writeByte(this.ordinal())
        );
    }

    @Override
    public String toString() {
        return "ServerError." + this.name();
    }
}
