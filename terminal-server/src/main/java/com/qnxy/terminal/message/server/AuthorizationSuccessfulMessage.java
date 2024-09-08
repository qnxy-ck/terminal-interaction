package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Mono;

/**
 * @param heartbeatInterval               指定终端心跳间隔 (秒)
 * @param maximumWaitSynchronousExecution 同步执行最大等待时间 (秒)
 * @author Qnxy
 */
public record AuthorizationSuccessfulMessage(
        byte heartbeatInterval,
        short maximumWaitSynchronousExecution
) implements ServerMessage {

    private static final byte INSTRUCTION_CODE = 0x01;

    @Override
    public Mono<ByteBuf> encode(ByteBufAllocator byteBufAllocator) {

        return this.simpleByteBuf(byteBufAllocator, INSTRUCTION_CODE, byteBuf -> {
            byteBuf.writeByte(this.heartbeatInterval);
            byteBuf.writeShort(this.maximumWaitSynchronousExecution);
        });

    }


}
