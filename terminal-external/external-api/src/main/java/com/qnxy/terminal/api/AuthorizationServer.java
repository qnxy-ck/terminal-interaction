package com.qnxy.terminal.api;

import reactor.core.publisher.Mono;

/**
 * @author Qnxy
 */
public interface AuthorizationServer {

    /**
     * 对指定参数进行授权, 如果可用则返回一个唯一id
     *
     * @param imei 设备 imei 编号
     * @return 失败抛出异常
     */
    Mono<Long> authorize(String imei);

}
