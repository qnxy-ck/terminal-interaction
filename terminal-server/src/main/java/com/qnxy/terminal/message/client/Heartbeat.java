package com.qnxy.terminal.message.client;

import com.qnxy.terminal.message.ClientMessage;
import lombok.ToString;

/**
 * @author Qnxy
 */
@ToString
public final class Heartbeat implements ProactiveMessages {

    public static final Heartbeat INSTANCE = new Heartbeat();

    private Heartbeat() {
    }

}
