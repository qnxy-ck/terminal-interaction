package com.qnxy.terminal.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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


}
