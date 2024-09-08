package com.qnxy.terminal.client;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Qnxy
 */
@Data
@Accessors(chain = true)
public class ClientContext {

    private final AtomicBoolean isAuth;
    private Long terminalId;
    private String imei;
    private Duration maxWaitMoveOutGoodsTime;

}
