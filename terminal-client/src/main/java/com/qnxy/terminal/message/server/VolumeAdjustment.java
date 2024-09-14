package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import io.netty.buffer.ByteBuf;

/**
 * 终端机器音量大小调节
 *
 * @author Qnxy
 */
public record VolumeAdjustment(
        byte volume
) implements ServerMessage {


    public static ServerMessage decode(ByteBuf buf) {
        byte volume = buf.readByte();
        return new VolumeAdjustment(volume);
    }
}
