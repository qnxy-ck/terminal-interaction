package com.qnxy.terminal.api;

import com.qnxy.terminal.api.data.SwipeCardCallbackReq;
import com.qnxy.terminal.api.data.SwipeCardResp;
import reactor.core.publisher.Mono;

/**
 * @author Qnxy
 */
public interface AuthorizedMoveOutGoodsService {

    /**
     * 刷卡调用接口
     *
     * @param terminalId 那台机器被刷卡
     * @param cardCode   卡的编号是什么
     * @return 成功返回出货相关信息, 失败则抛出异常
     */
    Mono<SwipeCardResp> swipeCard(long terminalId, String cardCode);
    
    Mono<Void> swipeCardCallback(SwipeCardCallbackReq swipeCardCallbackReq);

}
