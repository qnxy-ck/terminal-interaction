package com.qnxy.terminal.message.client;

import lombok.ToString;

/**
 * @author Qnxy
 */
@ToString
public final class SetupSuccessful implements CompleteMessage {

    public static final SetupSuccessful INSTANCE = new SetupSuccessful();

    private SetupSuccessful() {
    }


}
