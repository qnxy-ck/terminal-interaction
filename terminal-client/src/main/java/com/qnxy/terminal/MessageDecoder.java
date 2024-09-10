package com.qnxy.terminal;

import com.qnxy.terminal.message.ServerMessage;
import com.qnxy.terminal.message.ServerMessageType;
import com.qnxy.terminal.message.server.AuthorizationSuccessful;
import com.qnxy.terminal.message.server.AuthorizedMoveOutGoods;
import com.qnxy.terminal.message.server.ServerError;
import com.qnxy.terminal.message.server.Successful;
import io.netty.buffer.ByteBuf;

/**
 * @author Qnxy
 */
public class MessageDecoder {


    public static ServerMessage decode(ByteBuf byteBuf) {
        final short instructionCode = byteBuf.readUnsignedByte();
        final ServerMessageType messageType = ServerMessageType.valueOf((char) instructionCode);

        return decodeBody(byteBuf, messageType);
    }


    private static ServerMessage decodeBody(ByteBuf body, ServerMessageType messageType) {

        return switch (messageType) {
            case AUTHORIZATION_SUCCESSFUL -> AuthorizationSuccessful.decode(body);
            case SERVER_ERROR -> ServerError.decode(body);
            case SUCCESSFUL -> Successful.INSTANCE;
            case AUTHORIZED_MOVE_OUT_GOODS -> AuthorizedMoveOutGoods.decode(body);
            default -> throw new IllegalArgumentException("消息类型无效: " + messageType);
        };

    }
}
