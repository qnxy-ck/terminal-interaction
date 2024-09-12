package com.qnxy.terminal.message.client;

import com.qnxy.terminal.exceptions.IllegalDecodingException;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;

/**
 * 终端发出的错误消息
 * <p>
 * 终端再执行下发的指令是可能会检测到多个机器故障 无法执行
 *
 * @param errorCodes 故障码
 * @author Qnxy
 */
public record ErrorMessage(
        ErrorCode[] errorCodes
) implements CompleteMessage {

    /**
     * 消息解码
     */
    public static ErrorMessage decode(ByteBuf buffer) {
        final byte errorCount = buffer.readByte();

        final ErrorCode[] errorCodes = new ErrorCode[errorCount];

        for (byte i = 0; i < errorCount; i++) {
            byte errCode = buffer.readByte();
            final ErrorCode errorCode = ErrorCode.errCodeOf(errCode)
                    .orElseThrow(() -> new IllegalDecodingException("未知的错误编码: " + errCode));

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
