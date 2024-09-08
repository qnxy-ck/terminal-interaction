package com.qnxy.terminal.message.client;

import com.qnxy.terminal.ProtocolVersion;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * 通讯认证
 *
 * @author Qnxy
 */
public record Authentication(
        ProtocolVersion protocolVersion,
        String imei
) implements ProactiveMessages {

    public static Authentication decode(ByteBuf byteBuf) {
        final ProtocolVersion protocolVersion = ProtocolVersion.valueOf(byteBuf.readByte());

        final CharSequence imei = byteBuf.readCharSequence(byteBuf.readerIndex(), StandardCharsets.UTF_8);
        return new Authentication(protocolVersion, imei.toString());
    }
}
