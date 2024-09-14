package com.qnxy.terminal.external;

import com.qnxy.terminal.message.client.SwipeCardMethod;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author Qnxy
 */
@Slf4j
public class DefaultTerminalExternalService implements TerminalExternalService {

    @Override
    public Mono<AuthorizationInfo> authorize(String imei) {
        return Mono.just(new AuthorizationInfo(
                1L,
                Duration.ofSeconds(50),
                Duration.ofSeconds(10)
        ));
    }

    @Override
    public Mono<SwipeCardResp> swipeCard(long terminalId, SwipeCardMethod swipeCardMethod, String cardCode) {
        log.info("用户刷卡响应 {} {} {}", terminalId, cardCode, swipeCardMethod);
        return Mono.just(new SwipeCardResp(
                        100L,
                        (byte) 1,
                        true
                ))
                .delayElement(Duration.ofMillis(2000))
                ;
    }

    @Override
    public Mono<Void> swipeCardCallback(SwipeCardCallbackReq swipeCardCallbackReq) {

        log.info("收到回调结果 {}", swipeCardCallbackReq);
        return Mono.empty();
    }


}