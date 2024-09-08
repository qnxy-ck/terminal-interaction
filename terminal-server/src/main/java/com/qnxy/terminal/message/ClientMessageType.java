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
    ADJUSTMENT_SUCCESSFUL('S'),
    SWIPE_CARD('c'),
    ERROR_MESSAGE('E');


    private final char instructionCode;

    public static ClientMessageType valueOf(byte b) {
        return Arrays.stream(values())
                .filter(m -> m.instructionCode == b)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid message type: " + b));
    }

}
