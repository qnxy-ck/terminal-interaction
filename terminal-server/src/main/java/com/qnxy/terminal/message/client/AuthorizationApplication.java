package com.qnxy.terminal.message.client;

import com.qnxy.terminal.ProtocolVersion;
import com.qnxy.terminal.exceptions.IllegalDecodingException;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * 终端发起的终端认证申请
 *
 * @param protocolVersion 当前连接协议版本 {@link ProtocolVersion}
 * @param imei            该终端的设备编号
 * @author Qnxy
 */
@Slf4j
public record AuthorizationApplication(
        ProtocolVersion protocolVersion,
        String imei
) implements ProactiveSyncMessage {

    /**
     * 消息解码
     */
    public static AuthorizationApplication decode(ByteBuf byteBuf) {
        final byte versionNum = byteBuf.readByte();
        final ProtocolVersion protocolVersion = ProtocolVersion.valueOf(versionNum)
                .orElseThrow(() -> new IllegalDecodingException("未知协议版本: " + versionNum));

        final CharSequence imei = byteBuf.readCharSequence(byteBuf.readableBytes(), StandardCharsets.UTF_8);
        return new AuthorizationApplication(protocolVersion, imei.toString());
    }


}
