package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import com.qnxy.terminal.message.ServerMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * 最后的完成响应 响应心跳等
 *
 * @author Qnxy
 */
@ToString
public final class Complete implements ServerMessage {

    public static final Complete INSTANCE = new Complete();

    private Complete() {

    }

    @Override
    public Mono<ByteBuf> encode(ByteBufAllocator byteBufAllocator) {
        return this.simpleByteBuf(byteBufAllocator, ServerMessageType.COMPLETE);
    }

}
