package com.qnxy.terminal;

import com.qnxy.terminal.message.ServerMessage;
import com.qnxy.terminal.message.ServerMessageType;
import com.qnxy.terminal.message.server.*;
import io.netty.buffer.ByteBuf;

/**
 * @author Qnxy
 */
public class MessageDecoder {

    public static ServerMessage decode(ByteBuf buf) {

        short instructionCode = buf.readUnsignedByte();

        ServerMessageType serverMessageType = ServerMessageType.fromInstructionCode((char) instructionCode)
                .orElseThrow(() -> new RuntimeException("未知 server 消息类型: " + instructionCode));

        return decodeBody(serverMessageType, buf);
    }

    private static ServerMessage decodeBody(ServerMessageType serverMessageType, ByteBuf buf) {
        return switch (serverMessageType) {
            case COMPLETE -> Complete.INSTANCE;
            case AUTHORIZED_MOVE_OUT_GOODS -> AuthorizedMoveOutGoods.decode(buf);
            case AUTHORIZATION_APPLICATION_PASSED -> AuthorizationApplicationPassed.decode(buf);
            case VOLUME_ADJUSTMENT -> VolumeAdjustment.decode(buf);
            case SERVER_ERROR -> ServerError.decode(buf);
        };
    }

}
