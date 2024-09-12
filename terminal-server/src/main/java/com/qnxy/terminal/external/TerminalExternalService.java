package com.qnxy.terminal.external;

import com.qnxy.terminal.message.client.SwipeCardMethod;
import reactor.core.publisher.Mono;

import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * @author Qnxy
 */
public interface TerminalExternalService {

    TerminalExternalService INSTANCE = StreamSupport.stream(ServiceLoader.load(TerminalExternalService.class).spliterator(), false)
            .findFirst()
            .orElseGet(DefaultTerminalExternalService::new);


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
     * @param terminalId      那台机器被刷卡
     * @param swipeCardMethod 刷卡方式
     * @param cardCode        卡的编号是什么
     * @return 成功返回出货相关信息, 失败则抛出异常
     */
    Mono<SwipeCardResp> swipeCard(long terminalId, SwipeCardMethod swipeCardMethod, String cardCode);

    /**
     * 刷卡出货后的回调
     * 如果错误码不为空则出货失败, 否则为成功
     *
     * @param swipeCardCallbackReq 回调信息
     * @return .
     */
    Mono<Void> swipeCardCallback(SwipeCardCallbackReq swipeCardCallbackReq);

}
