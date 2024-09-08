package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.qnxy.terminal.message.ServerMessageType.AUTHORIZATION_SUCCESSFUL;

/**
 * @param heartbeatInterval               指定终端心跳间隔 (秒)
 * @param maximumWaitSynchronousExecution 同步执行最大等待时间 (秒)
 * @author Qnxy
 */
public record AuthorizationSuccessful(
        Duration heartbeatInterval
) implements ServerMessage {

    @Override
    public Mono<ByteBuf> encode(ByteBufAllocator byteBufAllocator) {
        return this.simpleByteBuf(byteBufAllocator, AUTHORIZATION_SUCCESSFUL, byteBuf -> {
            byteBuf.writeByte(Math.min((int) this.heartbeatInterval.toSeconds(), Byte.MAX_VALUE));
        });
    }

}
