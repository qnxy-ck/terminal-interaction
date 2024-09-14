package com.qnxy.terminal.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Mono;

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
