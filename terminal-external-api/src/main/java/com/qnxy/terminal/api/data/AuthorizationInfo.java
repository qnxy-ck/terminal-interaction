package com.qnxy.terminal.api.data;

import java.time.Duration;

/**
 * @author Qnxy
 */
public record AuthorizationInfo(
        Long terminalId, Duration
        terminalHeartbeatInterval,
        Duration synchronousExecutionMaximumWaitTime
) {

}
