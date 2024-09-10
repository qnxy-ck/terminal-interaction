package com.qnxy.terminal.message.client;

import com.qnxy.terminal.IllegalEncodingException;
import com.qnxy.terminal.api.data.GoodsTagsType;
import io.netty.buffer.ByteBuf;

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
) implements CompleteMessage {

    public static AuthorizedMoveOutGoodsReceipt decode(ByteBuf buffer) {
        final boolean alreadyTakenOut = buffer.readBoolean();
        final byte tagsTypeNum = buffer.readByte();

        final GoodsTagsType goodsTagsType = GoodsTagsType.typeNumberOf(tagsTypeNum)
                .orElseThrow(() -> new IllegalEncodingException("未知的标签类型: " + tagsTypeNum));

        final String tagsCode = goodsTagsType != GoodsTagsType.NOTHING
                ? buffer.readCharSequence(buffer.readableBytes(), StandardCharsets.UTF_8).toString()
                : null;

        return new AuthorizedMoveOutGoodsReceipt(alreadyTakenOut, goodsTagsType, tagsCode);
    }
}
