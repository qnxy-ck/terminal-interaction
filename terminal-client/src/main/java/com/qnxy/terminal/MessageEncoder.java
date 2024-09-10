package com.qnxy.terminal;

import com.qnxy.terminal.message.ClientMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * @author Qnxy
 */
@RequiredArgsConstructor
public final class MessageEncoder {

    private final ByteBufAllocator byteBufAllocator;

    public Mono<ByteBuf> encode(ClientMessage message) {
        return Mono.fromSupplier(() -> {
            final ByteBuf buf = message.encode(this.byteBufAllocator);

            final int readableBytes = buf.readableBytes();
            final ByteBuf out = byteBufAllocator.ioBuffer(Short.BYTES + readableBytes);

            out.writeShort(readableBytes);
            out.writeBytes(buf);
            return out;
        });
    }

}
