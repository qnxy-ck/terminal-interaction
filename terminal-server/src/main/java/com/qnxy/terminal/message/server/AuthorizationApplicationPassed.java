package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.qnxy.terminal.message.ServerMessageType.AUTHORIZATION_APPLICATION_PASSED;

/**
 * 终端授权申请通过消息
 * 
 * @param heartbeatInterval 指定终端心跳间隔 (秒)
 * @author Qnxy
 */
public record AuthorizationApplicationPassed(
        Duration heartbeatInterval
) implements ServerMessage {

    @Override
    public Mono<ByteBuf> encode(ByteBufAllocator byteBufAllocator) {
        return this.simpleByteBuf(
                byteBufAllocator,
                AUTHORIZATION_APPLICATION_PASSED,
                byteBuf -> byteBuf.writeByte(Math.min((int) this.heartbeatInterval.toSeconds(), Byte.MAX_VALUE))
        );
    }

}
