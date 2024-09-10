package com.qnxy.terminal.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * @author Qnxy
 */
@RequiredArgsConstructor
@Getter
public enum ServerMessageType {

    AUTHORIZED_MOVE_OUT_GOODS('F'),
    AUTHORIZATION_SUCCESSFUL('s'),
    SUCCESSFUL('c'),
    VOLUME_ADJUSTMENT('V'),
    SERVER_ERROR('E'),

    ;


    private final char instructionCode;

    public static ServerMessageType valueOf(char instructionCode) {
        return Arrays.stream(values())
                .filter(type -> type.instructionCode == instructionCode)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid ServerMessageType: " + instructionCode));
    }


}
