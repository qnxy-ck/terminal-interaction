package com.qnxy.terminal.message.client;

import com.qnxy.terminal.IllegalEncodingException;
import com.qnxy.terminal.api.data.ErrorCode;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;

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
            byte errCode = buffer.readByte();
            final ErrorCode errorCode = ErrorCode.errCodeOf(errCode)
                    .orElseThrow(() -> new IllegalEncodingException("未知的错误编码: " + errCode));

            errorCodes[i] = errorCode;
        }

        return new ErrorMessage(errorCodes);
    }

    @Override
    public String toString() {
        return "ErrorMessage{" +
                "errorCodes=" + Arrays.toString(errorCodes) +
                '}';
    }
}
