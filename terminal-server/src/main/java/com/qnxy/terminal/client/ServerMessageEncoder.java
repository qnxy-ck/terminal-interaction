package com.qnxy.terminal.client;

import com.qnxy.terminal.message.ServerMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * 服务端消息编码器, 在消息发出之前的操作
 *
 * @author Qnxy
 */
@RequiredArgsConstructor
public final class ServerMessageEncoder {

    private final ByteBufAllocator byteBufAllocator;

    public Mono<ByteBuf> encode(ServerMessage message) {
        return Mono.defer(() -> message.encode(this.byteBufAllocator)
                .map(it -> {
                    CompositeByteBuf out = byteBufAllocator.compositeBuffer();
                    out.writeShort(it.readableBytes());
                    out.writeBytes(it);
                    return out;
                })
        );
    }

}
