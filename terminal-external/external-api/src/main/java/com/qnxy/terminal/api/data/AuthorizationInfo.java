package com.qnxy.terminal.api.data;

import lombok.Data;

import java.time.Duration;

/**
 * @author Qnxy
 */
@Data
public class AuthorizationInfo {

    private final Long terminalId;
    private final Duration terminalHeartbeatInterval;
    private final Duration terminalWaitMaxMoveOutGoodsTime;


}
