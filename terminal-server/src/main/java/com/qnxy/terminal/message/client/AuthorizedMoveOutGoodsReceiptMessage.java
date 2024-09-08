package com.qnxy.terminal.message.client;

import com.qnxy.terminal.api.GoodsTagsType;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * 授权出货回执信息
 *
 * @author Qnxy
 */
public record AuthorizedMoveOutGoodsReceiptMessage(
        boolean alreadyTakenOut,
        GoodsTagsType tagsType,
        String tagsCode
) implements CompleteMessage {

    public static AuthorizedMoveOutGoodsReceiptMessage decode(ByteBuf buffer) {
        final boolean alreadyTakenOut = buffer.readBoolean();
        final GoodsTagsType goodsTagsType = GoodsTagsType.typeNumberOf(buffer.readByte())
                .orElseThrow(() -> new IllegalArgumentException("错误的标签类型"));

        final String tagsCode = buffer.readCharSequence(buffer.readerIndex(), StandardCharsets.UTF_8)
                .toString();

        return new AuthorizedMoveOutGoodsReceiptMessage(alreadyTakenOut, goodsTagsType, tagsCode);
    }
}
