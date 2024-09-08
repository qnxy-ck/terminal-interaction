package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import com.qnxy.terminal.message.ServerMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Mono;

/**
 * @author Qnxy
 */
public class Successful implements ServerMessage {

    public static final Successful INSTANCE = new Successful();

    @Override
    public Mono<ByteBuf> encode(ByteBufAllocator byteBufAllocator) {
        return this.simpleByteBuf(byteBufAllocator, ServerMessageType.SUCCESSFUL);
    }

}
