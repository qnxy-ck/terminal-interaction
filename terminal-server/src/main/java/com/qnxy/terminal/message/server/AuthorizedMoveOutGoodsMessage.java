package com.qnxy.terminal.message.server;

import com.qnxy.terminal.api.data.SwipeCardResp;
import com.qnxy.terminal.message.ServerMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Mono;

/**
 * 授权出货消息
 *
 * @param cargoLocation 货物位置
 * @param readTags      是否读取货物上的标签
 * @author Qnxy
 */
public record AuthorizedMoveOutGoodsMessage(
        byte cargoLocation,
        boolean readTags
) implements ServerMessage {

    private static final byte INSTRUCTION_CODE = 'F';

    @Override
    public Mono<ByteBuf> encode(ByteBufAllocator byteBufAllocator) {
        return this.simpleByteBuf(byteBufAllocator, INSTRUCTION_CODE, byteBuf -> {
            byteBuf.writeByte(this.cargoLocation);
            byteBuf.writeBoolean(this.readTags);
        });
    }

    public static AuthorizedMoveOutGoodsMessage withSwipeCardResp(SwipeCardResp swipeCardResp) {
        return new AuthorizedMoveOutGoodsMessage(
                swipeCardResp.cargoLocation(),
                swipeCardResp.readTags()
        );
    }

}
