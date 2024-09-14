package com.qnxy.terminal.client;

import com.qnxy.terminal.ServerContext;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 终端上下文信息
 *
 * @author Qnxy
 */
@Getter
public class ClientContext {

    /**
     * 当前终端是否认证
     */
    private final AtomicBoolean authorized;

    /**
     * 服务器信息
     */
    private final ServerContext serverContext;

    /**
     * 终端id
     */
    private Long terminalId;

    /**
     * 终端设备号
     */
    private String imei;



    public ClientContext(AtomicBoolean authorized, ServerContext serverContext) {
        this.authorized = authorized;
        this.serverContext = serverContext;
    }

    public void setAuthInfo(Long terminalId, String imei) {
        this.terminalId = terminalId;
        this.imei = imei;
    }


}
