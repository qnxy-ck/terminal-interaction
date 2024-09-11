package com.qnxy.terminal.message.client;

import lombok.ToString;

/**
 * 客户端心跳消息
 *
 * @author Qnxy
 */
@ToString
public final class Heartbeat implements ProactiveAsyncMessage {

    public static final Heartbeat INSTANCE = new Heartbeat();

    private Heartbeat() {
    }


}
