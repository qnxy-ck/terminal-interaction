package com.qnxy.terminal.message.server;

import com.qnxy.terminal.message.ServerMessage;
import lombok.ToString;

/**
 * @author Qnxy
 */
@ToString
public final class Successful implements ServerMessage {

    public static final Successful INSTANCE = new Successful();

    private Successful() {

    }

}
