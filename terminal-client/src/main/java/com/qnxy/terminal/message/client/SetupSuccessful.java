package com.qnxy.terminal.message.client;

import com.qnxy.terminal.message.ClientMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.ToString;

import static com.qnxy.terminal.message.ClientMessageType.SETUP_SUCCESSFUL;

/**
 * @author Qnxy
 */
@ToString
public final class SetupSuccessful implements ClientMessage {

    public static final SetupSuccessful INSTANCE = new SetupSuccessful();

    private SetupSuccessful() {
    }


    @Override
    public ByteBuf encode(ByteBufAllocator byteBufAllocator) {
        return this.simpleByteBuf(byteBufAllocator, SETUP_SUCCESSFUL);
    }
}
