package com.qnxy.terminal.message.client;

import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.ClientMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * @author Qnxy
 */
public record ErrorMessage(
        ErrorCode[] errorCodes
) implements ClientMessage {

    public static ErrorMessage decode(ByteBuf buffer) {
        final byte errorCount = buffer.readByte();

        final ErrorCode[] errorCodes = new ErrorCode[errorCount];

        for (byte i = 0; i < errorCount; i++) {
            final ErrorCode errorCode = ErrorCode.errCodeOf(buffer.readByte())
                    .orElseThrow(() -> new RuntimeException("不存在的错误编码"));

            errorCodes[i] = errorCode;
        }

        return new ErrorMessage(errorCodes);
    }

    @Override
    public ByteBuf encode(ByteBufAllocator byteBufAllocator) {
        return this.simpleByteBuf(
                byteBufAllocator,
                ClientMessageType.ERROR_MESSAGE,
                byteBuf -> {
                    byteBuf.writeByte(this.errorCodes.length);
                    for (ErrorCode errorCode : this.errorCodes) {
                        byteBuf.writeByte(errorCode.ordinal());
                    }
                }
        );
    }
}
