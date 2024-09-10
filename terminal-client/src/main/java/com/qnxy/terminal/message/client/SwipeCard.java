package com.qnxy.terminal.message.client;

import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.ClientMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author Qnxy
 */
@Slf4j
public record SwipeCard(
        SwipeCardMethod swipeCardMethod,
        String cardCode
) implements ClientMessage {

    @Override
    public ByteBuf encode(ByteBufAllocator byteBufAllocator) {
        return this.simpleByteBuf(
                byteBufAllocator,
                ClientMessageType.SWIPE_CARD,
                byteBuf -> {
                    byteBuf.writeByte(this.swipeCardMethod.ordinal());
                    byteBuf.writeCharSequence(this.cardCode, StandardCharsets.UTF_8);
                }
        );
    }
}
