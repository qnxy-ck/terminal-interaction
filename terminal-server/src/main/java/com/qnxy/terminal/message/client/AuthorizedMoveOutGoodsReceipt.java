package com.qnxy.terminal.message.client;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * 授权出货后终端返回的授权出货回执信息
 * <p>
 * 如果失败将返回 {@link ErrorMessage}
 *
 * @param alreadyTakenOut 出货完成后用户是否取出
 * @param tagsCode        识别到的该货物编码
 * @author Qnxy
 */
public record AuthorizedMoveOutGoodsReceipt(
        boolean alreadyTakenOut,
        String tagsCode
) implements CompleteMessage {

    public static AuthorizedMoveOutGoodsReceipt decode(ByteBuf buffer) {
        final boolean alreadyTakenOut = buffer.readBoolean();

        final String tagsCode = buffer.readCharSequence(buffer.readableBytes(), StandardCharsets.UTF_8).toString();
        return new AuthorizedMoveOutGoodsReceipt(alreadyTakenOut, tagsCode);
    }
}
