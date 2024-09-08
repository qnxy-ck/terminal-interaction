package com.qnxy.terminal.message.client;

import com.qnxy.terminal.api.data.ErrorCode;
import io.netty.buffer.ByteBuf;

/**
 * @author Qnxy
 */
public record ErrorMessage(
        ErrorCode[] errorCodes
) implements CompleteMessage {

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
}
