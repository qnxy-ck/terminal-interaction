package com.qnxy.terminal.message.client;

import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.ClientMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.StandardCharsets;

/**
 * 授权出货回执信息
 *
 * @author Qnxy
 */
public record AuthorizedMoveOutGoodsReceipt(
        boolean alreadyTakenOut,
        GoodsTagsType tagsType,
        String tagsCode
) implements ClientMessage {


    @Override
    public ByteBuf encode(ByteBufAllocator byteBufAllocator) {
        return this.simpleByteBuf(
                byteBufAllocator,
                ClientMessageType.AUTHORIZED_MOVE_OUT_GOODS_RECEIPT,
                byteBuf -> {
                    byteBuf.writeBoolean(this.alreadyTakenOut);
                    byteBuf.writeByte(this.tagsType.ordinal());

                    if (tagsType != GoodsTagsType.NOTHING) {
                        byteBuf.writeCharSequence(this.tagsCode, StandardCharsets.UTF_8);
                    }
                }
        );
    }
}
