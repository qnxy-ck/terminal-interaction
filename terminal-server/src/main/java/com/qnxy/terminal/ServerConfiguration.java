package com.qnxy.terminal;

import java.time.Duration;

/**
 * @param port                                          服务器端口
 * @param maximumAuthorizationWait                      等待授权消息最大时间
 * @param waitAfterAuthorizationIsPassed                授权通过消息发送后等待响应时间
 * @param swipeCardAuthMoveOutGoodsCallbackRetryCount   用户刷卡授权出货成功后回调服务失败重试次数
 * @param swipeCardAuthMoveOutGoodsCallbackRetryBackoff 用户刷卡授权出货成功后回调服务失败重试回退时间
 * @author Qnxy
 */
public record ServerConfiguration(
        int port,
        Duration maximumAuthorizationWait,
        Duration waitAfterAuthorizationIsPassed,
        int swipeCardAuthMoveOutGoodsCallbackRetryCount,
        Duration swipeCardAuthMoveOutGoodsCallbackRetryBackoff
) {

}
