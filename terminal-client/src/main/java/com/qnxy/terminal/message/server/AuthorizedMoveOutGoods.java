package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import io.netty.buffer.ByteBuf;

/**
 * 授权出货消息
 *
 * @param cargoLocation 货物位置
 * @param readTags      是否读取货物上的标签
 * @author Qnxy
 */
public record AuthorizedMoveOutGoods(
        byte cargoLocation,
        boolean readTags
) implements ServerMessage {


    public static AuthorizedMoveOutGoods decode(ByteBuf buffer) {

        byte cargoLocation = buffer.readByte();
        boolean readTags = buffer.readBoolean();
        return new AuthorizedMoveOutGoods(cargoLocation, readTags);
    }

}
