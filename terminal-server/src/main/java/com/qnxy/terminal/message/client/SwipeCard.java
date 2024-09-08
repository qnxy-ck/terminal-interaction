package com.qnxy.terminal.message.client;

import com.qnxy.terminal.api.data.SwipeCardMethod;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * @author Qnxy
 */
public record SwipeCard(
        SwipeCardMethod swipeCardMethod,
        String cardCode
) implements ProactiveMessages {

    public static SwipeCard decode(ByteBuf buffer) {
        final byte typeNumber = buffer.readByte();

        final SwipeCardMethod swipeCardMethod = SwipeCardMethod.typeNumberOf(typeNumber)
                .orElseThrow(() -> new IllegalArgumentException("Unknown SwipeCard type: " + typeNumber));

        final String cardCode = buffer.readCharSequence(buffer.readerIndex(), StandardCharsets.UTF_8)
                .toString();

        return new SwipeCard(swipeCardMethod, cardCode);
    }
}
