package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import lombok.ToString;

/**
 * @author Qnxy
 */
@ToString
public final class Complete implements ServerMessage {

    public static final Complete INSTANCE = new Complete();

    private Complete() {
    }
}
