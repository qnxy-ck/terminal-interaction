package com.qnxy.terminal.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.function.Consumer;

/**
 * @author Qnxy
 */
public interface ClientMessage {


    ByteBuf encode(ByteBufAllocator byteBufAllocator);


    default ByteBuf simpleByteBuf(ByteBufAllocator byteBufAllocator, ClientMessageType messageType) {
        return byteBufAllocator.ioBuffer(1).writeByte(messageType.getInstructionCode());
    }

    default ByteBuf simpleByteBuf(ByteBufAllocator byteBufAllocator, ClientMessageType messageType, Consumer<ByteBuf> consumer) {
        final ByteBuf byteBuf = byteBufAllocator.ioBuffer();

        byteBuf.writeByte(messageType.getInstructionCode());
        consumer.accept(byteBuf);
        return byteBuf;
    }

}
