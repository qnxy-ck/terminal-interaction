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


    default Mono<ByteBuf> simpleByteBuf(ByteBufAllocator byteBufAllocator, byte instructionCode) {
        return Mono.fromSupplier(() -> byteBufAllocator.ioBuffer(1).writeByte(instructionCode));
    }
    
    default Mono<ByteBuf> simpleByteBuf(ByteBufAllocator byteBufAllocator, byte instructionCode, Consumer<ByteBuf> consumer) {
        return Mono.fromSupplier(() -> {
            final ByteBuf byteBuf = byteBufAllocator.ioBuffer();
            
            byteBuf.writeByte(instructionCode);
            consumer.accept(byteBuf);
            return byteBuf;
        });
    }
}
