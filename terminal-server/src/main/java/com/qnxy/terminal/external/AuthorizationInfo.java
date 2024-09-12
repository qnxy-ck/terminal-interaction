package com.qnxy.terminal.external;

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
