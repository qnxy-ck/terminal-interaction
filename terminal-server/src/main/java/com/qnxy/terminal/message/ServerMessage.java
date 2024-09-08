package com.qnxy.terminal.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/**
 * @author Qnxy
 */
public interface ServerMessage {

    Mono<ByteBuf> encode(ByteBufAllocator byteBufAllocator);


    default Mono<ByteBuf> simpleByteBuf(ByteBufAllocator byteBufAllocator, ServerMessageType serverMessageType) {
        return Mono.fromSupplier(() -> byteBufAllocator.ioBuffer(1).writeByte(serverMessageType.getInstructionCode()));
    }

    default Mono<ByteBuf> simpleByteBuf(ByteBufAllocator byteBufAllocator, ServerMessageType serverMessageType, Consumer<ByteBuf> consumer) {
        return Mono.fromSupplier(() -> {
            final ByteBuf byteBuf = byteBufAllocator.ioBuffer();

            byteBuf.writeByte(serverMessageType.getInstructionCode());
            consumer.accept(byteBuf);
            return byteBuf;
        });
    }
}
