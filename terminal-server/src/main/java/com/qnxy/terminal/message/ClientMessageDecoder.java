package com.qnxy.terminal.message;

import com.qnxy.terminal.message.client.*;
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
            case CONNECT_AUTHENTICATION -> ConnectAuthentication.decode(body);
            case HEARTBEAT -> Heartbeat.INSTANCE;
            case ADJUSTMENT_SUCCESSFUL -> AdjustmentSuccessful.INSTANCE;
            case SWIPE_CARD -> SwipeCard.decode(body);
            case ERROR_MESSAGE -> ErrorMessage.decode(body);
            case AUTHORIZED_MOVE_OUT_GOODS_RECEIPT -> AuthorizedMoveOutGoodsReceipt.decode(body);
        };
    }


}
