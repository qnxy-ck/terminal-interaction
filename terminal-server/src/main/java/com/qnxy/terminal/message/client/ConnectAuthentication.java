package com.qnxy.terminal.message.client;

import com.qnxy.terminal.IllegalEncodingException;
import com.qnxy.terminal.ProtocolVersion;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * 通讯认证
 *
 * @author Qnxy
 */
@Slf4j
public record ConnectAuthentication(
        ProtocolVersion protocolVersion,
        String imei
) implements ProactiveSyncMessage {

    /**
     * 消息解码
     */
    public static ConnectAuthentication decode(ByteBuf byteBuf) {
        final byte versionNum = byteBuf.readByte();
        final ProtocolVersion protocolVersion = ProtocolVersion.valueOf(versionNum)
                .orElseThrow(() -> new IllegalEncodingException("未知协议版本: " + versionNum));

        final CharSequence imei = byteBuf.readCharSequence(byteBuf.readableBytes(), StandardCharsets.UTF_8);
        return new ConnectAuthentication(protocolVersion, imei.toString());
    }


}
