package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Mono;

import static com.qnxy.terminal.message.ServerMessageType.VOLUME_ADJUSTMENT;

/**
 * @author Qnxy
 */
public record VolumeAdjustment(
        byte volume
) implements ServerMessage {

    @Override
    public Mono<ByteBuf> encode(ByteBufAllocator byteBufAllocator) {
        return this.simpleByteBuf(
                byteBufAllocator,
                VOLUME_ADJUSTMENT,
                byteBuf -> byteBuf.writeByte(volume)
        );
    }
}
