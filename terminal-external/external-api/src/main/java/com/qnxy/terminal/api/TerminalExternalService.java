package com.qnxy.terminal.api;

import com.qnxy.terminal.api.data.AuthorizationInfo;
import com.qnxy.terminal.api.data.SwipeCardCallbackReq;
import com.qnxy.terminal.api.data.SwipeCardResp;
import reactor.core.publisher.Mono;

import java.util.ServiceLoader;

/**
 * @author Qnxy
 */
public interface TerminalExternalService {

    TerminalExternalService INSTANCE = ServiceLoader.load(TerminalExternalService.class).iterator().next();


    /**
     * 对指定参数进行授权, 如果可用则返回一个唯一id
     *
     * @param imei 设备 imei 编号
     * @return 失败抛出异常
     */
    Mono<AuthorizationInfo> authorize(String imei);


    /**
     * 刷卡调用接口
     *
     * @param terminalId 那台机器被刷卡
     * @param cardCode   卡的编号是什么
     * @return 成功返回出货相关信息, 失败则抛出异常
     */
    Mono<SwipeCardResp> swipeCard(long terminalId, String cardCode);

    /**
     * 刷卡出货后的回调
     * 如果错误码不为空则出货失败, 否则为成功
     *
     * @param swipeCardCallbackReq 回调信息
     * @return .
     */
    Mono<Void> swipeCardCallback(SwipeCardCallbackReq swipeCardCallbackReq);

    /**
     * 音量调节回调
     *
     * @param successful 是否成功, 成功为true
     * @return .
     */
    Mono<Void> volumeAdjustmentCallback(boolean successful);

}
