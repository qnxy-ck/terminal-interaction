package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.ToString;

/**
 * @author Qnxy
 */
@ToString
public final class Quit implements ServerMessage {

    public static final Quit INSTANCE = new Quit();

    private Quit() {

    }

    @Override
    public ByteBuf encode(ByteBufAllocator byteBufAllocator) {
        return null;
    }

}
