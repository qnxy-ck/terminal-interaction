package com.qnxy.terminal.message.client;

import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.ClientMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.StandardCharsets;

/**
 * @author Qnxy
 */
public record ConnectAuthentication(
        byte protocolVersion,
        String imei
) implements ClientMessage {

    @Override
    public ByteBuf encode(ByteBufAllocator byteBufAllocator) {
        return this.simpleByteBuf(
                byteBufAllocator,
                ClientMessageType.CONNECT_AUTHENTICATION,
                byteBuf -> {
                    byteBuf.writeByte(this.protocolVersion);
                    byteBuf.writeCharSequence(this.imei, StandardCharsets.UTF_8);
                });
    }
}
