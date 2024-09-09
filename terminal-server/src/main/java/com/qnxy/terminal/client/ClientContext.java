package com.qnxy.terminal.client;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 上下文信息
 * 
 * @author Qnxy
 */
@Data
@Accessors(chain = true)
public class ClientContext {

    /**
     * 当前终端是否认证
     */
    private final AtomicBoolean isAuth;

    /**
     * 终端id
     */
    private Long terminalId;

    /**
     * 终端设备号
     */
    private String imei;

    /**
     * 同步消息执行最大等待时间
     */
    private Duration synchronousExecutionMaximumWaitTime;

}
