package com.qnxy.terminal.client;

import com.qnxy.terminal.ServerContext;
import lombok.Getter;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 上下文信息
 *
 * @author Qnxy
 */
@Getter
public class ClientContext {

    /**
     * 当前终端是否认证
     */
    private final AtomicBoolean authorized;
    private final ServerContext serverContext;

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

    public ClientContext(AtomicBoolean authorized, ServerContext serverContext) {
        this.authorized = authorized;
        this.serverContext = serverContext;
    }

    public void setAuthInfo(Long terminalId, String imei, Duration synchronousExecutionMaximumWaitTime) {
        this.terminalId = terminalId;
        this.imei = imei;
        this.synchronousExecutionMaximumWaitTime = synchronousExecutionMaximumWaitTime;
    }


}
