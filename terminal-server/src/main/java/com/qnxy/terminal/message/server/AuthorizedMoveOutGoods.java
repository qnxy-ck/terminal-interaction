package com.qnxy.terminal.message.server;

import com.qnxy.terminal.external.SwipeCardResp;
import com.qnxy.terminal.message.ServerMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Mono;

import static com.qnxy.terminal.message.ServerMessageType.AUTHORIZED_MOVE_OUT_GOODS;

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


    public static AuthorizedMoveOutGoods withSwipeCardResp(SwipeCardResp swipeCardResp) {
        return new AuthorizedMoveOutGoods(
                swipeCardResp.cargoLocation(),
                swipeCardResp.readTags()
        );
    }

    @Override
    public Mono<ByteBuf> encode(ByteBufAllocator byteBufAllocator) {
        return this.simpleByteBuf(
                byteBufAllocator,
                AUTHORIZED_MOVE_OUT_GOODS,
                byteBuf -> {
                    byteBuf.writeByte(this.cargoLocation);
                    byteBuf.writeBoolean(this.readTags);
                }
        );
    }

}
