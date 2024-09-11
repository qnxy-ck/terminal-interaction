package com.qnxy.terminal.message;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * @author Qnxy
 */
@RequiredArgsConstructor
public enum ClientMessageType {

    CONNECT_AUTHENTICATION('A'),
    HEARTBEAT('P'),
    AUTHORIZED_MOVE_OUT_GOODS_RECEIPT('M'),
    SETUP_SUCCESSFUL('S'),
    SWIPE_CARD('c'),
    ERROR_MESSAGE('E');


    private final char instructionCode;

    public static ClientMessageType valueOf(char instructionCode) {
        return Arrays.stream(values())
                .filter(m -> m.instructionCode == instructionCode)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid message type: " + instructionCode));
    }

}
