package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Mono;

/**
 * @author Qnxy
 */
public class SuccessfulMessage implements ServerMessage {

    public static final SuccessfulMessage INSTANCE = new SuccessfulMessage();
    private static final byte instructionCode = 0x0C;

    @Override
    public Mono<ByteBuf> encode(ByteBufAllocator byteBufAllocator) {
        return this.simpleByteBuf(byteBufAllocator, instructionCode);
    }

}
