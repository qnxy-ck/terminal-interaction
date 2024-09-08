package com.qnxy.terminal;

import java.time.Duration;

/**
 * @param port                     服务器端口
 * @param maximumAuthorizationWait 等待授权消息最大时间
 * @author Qnxy
 */
public record ServerConfiguration(
        int port,
        Duration maximumAuthorizationWait
) {

}
