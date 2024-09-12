package com.qnxy.terminal.message;

import com.qnxy.terminal.exceptions.IllegalDecodingException;
import com.qnxy.terminal.message.client.ErrorMessage;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 客户端消息的类型
 * 
 * @author Qnxy
 */
@RequiredArgsConstructor
public enum ClientMessageType {

    /**
     * 授权申请
     */
    AUTHORIZATION_APPLICATION('A'),

    /**
     * 心跳
     */
    HEARTBEAT('P'),

    /**
     * 授权出货回执信息
     */
    AUTHORIZED_MOVE_OUT_GOODS_RECEIPT('M'),

    /**
     * 设置成功消息
     */
    SETUP_SUCCESSFUL('S'),

    /**
     * 刷卡消息
     */
    SWIPE_CARD('c'),

    /**
     * 错误消息 {@link ErrorMessage}
     */
    ERROR_MESSAGE('E'),
    ;


    private final char instructionCode;

    public static ClientMessageType valueOf(char instructionCode) {
        return Arrays.stream(values())
                .filter(m -> m.instructionCode == instructionCode)
                .findFirst()
                .orElseThrow(() -> new IllegalDecodingException("消息类型无效: " + instructionCode));
    }

}
