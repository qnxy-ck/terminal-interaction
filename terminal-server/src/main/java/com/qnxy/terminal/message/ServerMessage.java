package com.qnxy.terminal.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.function.Consumer;

/**
 * @author Qnxy
 */
public interface ServerMessage {

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
