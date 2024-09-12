package com.qnxy.terminal.message.client;

import com.qnxy.terminal.api.data.SwipeCardMethod;
import com.qnxy.terminal.exceptions.IllegalDecodingException;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * 终端刷卡消息
 *
 * @param swipeCardMethod 刷卡方式
 * @param cardCode        卡的编码
 * @author Qnxy
 */
public record SwipeCard(
        SwipeCardMethod swipeCardMethod,
        String cardCode
) implements ProactiveSyncMessage {

    /**
     * 解码终端发送的消息为当前类型
     */
    public static SwipeCard decode(ByteBuf buffer) {
        final byte typeNumber = buffer.readByte();

        final SwipeCardMethod swipeCardMethod = SwipeCardMethod.typeNumberOf(typeNumber)
                .orElseThrow(() -> new IllegalDecodingException("未知刷卡类型: " + typeNumber));

        final String cardCode = buffer.readCharSequence(buffer.readableBytes(), StandardCharsets.UTF_8).toString();
        return new SwipeCard(swipeCardMethod, cardCode);
    }

}
