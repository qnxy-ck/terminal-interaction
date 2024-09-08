package com.qnxy.terminal;

import java.time.Duration;

/**
 * @author Qnxy
 */
public record ServerConfiguration(
        int port,
        Duration maximumAuthorizationWait
) {

}
