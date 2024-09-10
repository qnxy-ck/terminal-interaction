package com.qnxy.terminal.message.client;

import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.ClientMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.ToString;

/**
 * @author Qnxy
 */
@ToString
public final class Heartbeat implements ClientMessage {

    public static final Heartbeat INSTANCE = new Heartbeat();

    private Heartbeat() {
    }

    @Override
    public ByteBuf encode(ByteBufAllocator byteBufAllocator) {
        return this.simpleByteBuf(byteBufAllocator, ClientMessageType.HEARTBEAT);
    }
}
