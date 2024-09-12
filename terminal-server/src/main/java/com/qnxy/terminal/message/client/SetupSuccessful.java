package com.qnxy.terminal.message.client;

import lombok.ToString;

/**
 * 终端执行完成后的响应
 * 
 * @author Qnxy
 */
@ToString
public final class SetupSuccessful implements CompleteMessage {

    public static final SetupSuccessful INSTANCE = new SetupSuccessful();

    private SetupSuccessful() {
    }


}
