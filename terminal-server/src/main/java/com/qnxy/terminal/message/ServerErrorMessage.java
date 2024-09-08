package com.qnxy.terminal.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * @author Qnxy
 */
@RequiredArgsConstructor
public enum ServerErrorMessage implements ServerMessage {

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
    ;


    private static final byte instructionCode = 0x0E;


    @Override
    public Mono<ByteBuf> encode(ByteBufAllocator byteBufAllocator) {
        return Mono.fromSupplier(() -> {
            final ByteBuf byteBuf = byteBufAllocator.ioBuffer(2);
            byteBuf.writeByte(instructionCode);
            byteBuf.writeByte(this.ordinal());
            return byteBuf;
        });
    }
}
