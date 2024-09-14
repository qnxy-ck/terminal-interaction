package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import io.netty.buffer.ByteBuf;

import java.time.Duration;

/**
 * 终端授权申请通过消息
 *
 * @param heartbeatInterval 指定终端心跳间隔 (秒)
 * @author Qnxy
 */
public record AuthorizationApplicationPassed(
        Duration heartbeatInterval
) implements ServerMessage {


    public static AuthorizationApplicationPassed decode(ByteBuf buffer) {

        short i = buffer.readUnsignedByte();
        Duration heartbeatInterval = Duration.ofSeconds(i);
        return new AuthorizationApplicationPassed(heartbeatInterval);
    }

}
