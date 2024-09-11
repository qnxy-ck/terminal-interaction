package com.qnxy.terminal.client;

import com.qnxy.terminal.message.ServerMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * @author Qnxy
 */
@RequiredArgsConstructor
public final class ServerMessageEncoder {

    private final ByteBufAllocator byteBufAllocator;

    public Mono<ByteBuf> encode(ServerMessage message) {
        return Mono.fromSupplier(() -> {
            final ByteBuf buf = message.encode(this.byteBufAllocator);

            final ByteBuf out = byteBufAllocator.ioBuffer(Short.BYTES + buf.readableBytes());

            out.writeShort(buf.readableBytes());
            out.writeBytes(buf);
            return out;
        });
    }

}
