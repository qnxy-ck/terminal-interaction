package com.qnxy.terminal.message;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Qnxy
 */
@RequiredArgsConstructor
public enum ClientMessageType {


    AUTHENTICATION('A'),
    HEARTBEAT('P'),
    ;


    private final char instructionCode;

    public static ClientMessageType valueOf(byte b) {
        return Arrays.stream(values())
                .filter(m -> m.instructionCode == b)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid message type: " + b));
    }
    
}
