package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import com.qnxy.terminal.message.ServerMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.ToString;

/**
 * @author Qnxy
 */
@ToString
public final class Successful implements ServerMessage {

    public static final Successful INSTANCE = new Successful();

    private Successful() {

    }

    @Override
    public ByteBuf encode(ByteBufAllocator byteBufAllocator) {
        return this.simpleByteBuf(byteBufAllocator, ServerMessageType.SUCCESSFUL);
    }

}
