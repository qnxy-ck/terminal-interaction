package com.qnxy.terminal.client;

import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.ClientMessageType;
import com.qnxy.terminal.message.client.*;
import io.netty.buffer.ByteBuf;

/**
 * @author Qnxy
 */
public final class ClientMessageDecoder {


    public static ClientMessage decode(ByteBuf byteBuf) {
        final short instructionCode = byteBuf.readUnsignedByte();
        final ClientMessageType messageType = ClientMessageType.valueOf((char) instructionCode);

        return decodeBody(byteBuf, messageType);
    }


    private static ClientMessage decodeBody(ByteBuf body, ClientMessageType messageType) {
        return switch (messageType) {
            case CONNECT_AUTHENTICATION -> ConnectAuthentication.decode(body);
            case HEARTBEAT -> Heartbeat.INSTANCE;
            case SETUP_SUCCESSFUL -> SetupSuccessful.INSTANCE;
            case SWIPE_CARD -> SwipeCard.decode(body);
            case ERROR_MESSAGE -> ErrorMessage.decode(body);
            case AUTHORIZED_MOVE_OUT_GOODS_RECEIPT -> AuthorizedMoveOutGoodsReceipt.decode(body);
        };
    }


}
