package com.qnxy.terminal.message;

import com.qnxy.terminal.message.client.Authentication;
import com.qnxy.terminal.message.client.Heartbeat;
import io.netty.buffer.ByteBuf;

/**
 * @author Qnxy
 */
public final class ClientMessageDecoder {


    public static ClientMessage decode(ByteBuf byteBuf) {
        final ClientMessageType messageType = ClientMessageType.valueOf(byteBuf.readByte());

        return decodeBody(byteBuf, messageType);
    }


    private static ClientMessage decodeBody(ByteBuf body, ClientMessageType messageType) {
        return switch (messageType) {
            case AUTHENTICATION -> Authentication.decode(body);
            case HEARTBEAT -> Heartbeat.INSTANCE;
        };
    }


}
