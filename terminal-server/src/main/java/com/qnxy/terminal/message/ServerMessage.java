package com.qnxy.terminal.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.function.Consumer;

/**
 * 服务端消息类型标志
 *
 * @author Qnxy
 */
public interface ServerMessage {

    /**
     * 服务器端消息编码操作
     */
    ByteBuf encode(ByteBufAllocator byteBufAllocator);


    default ByteBuf simpleByteBuf(ByteBufAllocator byteBufAllocator, ServerMessageType serverMessageType) {
        return byteBufAllocator.ioBuffer(1).writeByte(serverMessageType.getInstructionCode());
    }

    default ByteBuf simpleByteBuf(ByteBufAllocator byteBufAllocator, ServerMessageType serverMessageType, Consumer<ByteBuf> consumer) {
        final ByteBuf byteBuf = byteBufAllocator.ioBuffer();

        byteBuf.writeByte(serverMessageType.getInstructionCode());
        consumer.accept(byteBuf);
        return byteBuf;
    }
}
