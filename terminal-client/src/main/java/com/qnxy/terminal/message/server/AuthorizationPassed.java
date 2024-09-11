package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import io.netty.buffer.ByteBuf;

import java.time.Duration;

/**
 * @param heartbeatInterval               指定终端心跳间隔 (秒)
 * @param maximumWaitSynchronousExecution 同步执行最大等待时间 (秒)
 * @author Qnxy
 */
public record AuthorizationPassed(
        Duration heartbeatInterval
) implements ServerMessage {

    public static AuthorizationPassed decode(ByteBuf buffer) {
        byte b = buffer.readByte();
        return new AuthorizationPassed(Duration.ofSeconds(b));
    }

}
