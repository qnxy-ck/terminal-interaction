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
    SUCCESSFUL('C'),
    VOLUME_ADJUSTMENT('V');


    private final char instructionCode;


}
