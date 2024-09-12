package com.qnxy.terminal.processor;

import com.qnxy.terminal.ClientManager;
import com.qnxy.terminal.ProactiveSyncMessageProcessor;
import com.qnxy.terminal.api.TerminalExternalService;
import com.qnxy.terminal.client.ClientContext;
import com.qnxy.terminal.client.TerminalClient;
import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.client.AuthorizationApplication;
import com.qnxy.terminal.message.client.SetupSuccessful;
import com.qnxy.terminal.message.server.AuthorizationApplicationPassed;
import com.qnxy.terminal.message.server.ServerError;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * 终端认证申请同步处理器
 * <p>
 * 执行流程:
 * <p>
 * 1. 验证当前连接是否已认证
 * 如果已认证则判定为重复认证, 返回消息 {@link ServerError#UNEXPECTED_MESSAGE_ERROR} 且关闭该终端连接.
 * 未认证则取消当前终端未授权延时关闭任务
 * <p>
 * 2. 调用外部授权服务 {@link TerminalExternalService#authorize(String)}
 * 如果同意授权则根据返回信息查询当前终端连接是否已存在, 如果不存在则返回 {@link AuthorizationApplicationPassed}.
 * 否则返回消息 {@link ServerError#UNEXPECTED_MESSAGE_ERROR} 且关闭该终端连接
 * <p>
 * 3. 发送授权通过后等待终端响应 {@link SetupSuccessful}, 等待指定时间后: 成功响应后则保存该连接信息
 * 否则返回消息 {@link ServerError#UNEXPECTED_MESSAGE_ERROR} 且关闭该终端连接
 *
 * @author Qnxy
 */
@Slf4j
public class AuthorizationApplicationProactiveSyncMessageProcessor implements ProactiveSyncMessageProcessor<AuthorizationApplication> {

    @Override
    public Mono<Void> handle(TerminalClient client, AuthorizationApplication authorizationApplication, Flux<ClientMessage> responseMessageFlux) {
        return Mono.deferContextual(ctx -> {
            final ClientContext clientContext = ctx.get(ClientContext.class);

            if (clientContext.getAuthorized().compareAndSet(false, true)) {
                client.cancelClientDelayedClose();
                return doAuthorize(client, authorizationApplication, responseMessageFlux, clientContext);
            }

            log.error("重复的认证消息, 当前已经认证: {} -> {}", clientContext.getTerminalId(), authorizationApplication);

            // 重复认证, 直接关闭该终端连接 
            return client.close(ServerError.UNEXPECTED_MESSAGE_ERROR);
        });

    }

    private Mono<Void> doAuthorize(TerminalClient terminalClient, AuthorizationApplication authorizationApplication, Flux<ClientMessage> responseMessageFlux, ClientContext clientContext) {
        return clientContext.getServerContext()
                .terminalExternalService()
                .authorize(authorizationApplication.imei())
                .onErrorResume(e -> {
                    log.error("授权过程错误: {}", authorizationApplication.imei(), e);
                    return terminalClient.close(ServerError.CONNECTION_REFUSED_ERROR).then(Mono.empty());
                })
                .flatMap(it -> {
                    // 判断当前连接是否已经存在
                    // 如果存在则直接关闭当前连接
                    final Long terminalId = it.terminalId();
                    if (ClientManager.findClient(terminalId).isPresent()) {
                        log.error("该连接已存在无法建立, 当前连接已被关闭. IMEI: [{}] -- ID: [{}]", authorizationApplication.imei(), terminalId);
                        return terminalClient.close(ServerError.CONNECTION_REFUSED_ERROR);
                    }

                    // 发送认证成功的消息
                    terminalClient.send(new AuthorizationApplicationPassed(it.terminalHeartbeatInterval()));

                    return this.waitTerminalSuccessResp(responseMessageFlux, terminalClient, clientContext.getServerContext().serverConfiguration().waitAfterAuthorizationIsPassed())
                            .doOnNext(successful -> {
                                clientContext.setAuthInfo(
                                        terminalId,
                                        authorizationApplication.imei(),
                                        it.synchronousExecutionMaximumWaitTime()
                                );

                                // 心跳超时时间为每次心跳间隔的三倍
                                // 超出该时间将被断开连接
                                final Duration readIdleDuration = Duration.ofMillis(it.terminalHeartbeatInterval().toMillis() * 3);
                                terminalClient.registerReadIdle(readIdleDuration);

                                // 添加到客户端管理器中 
                                ClientManager.addClient(terminalId, terminalClient);
                            });
                })
                .then();
    }


    public Mono<SetupSuccessful> waitTerminalSuccessResp(Flux<ClientMessage> responseMessageFlux, TerminalClient terminalClient, Duration waitAfterAuthorizationIsPassed) {
        return responseMessageFlux.last()
                .flatMap(it -> {
                    if (it instanceof SetupSuccessful setupSuccessful) {
                        return Mono.just(setupSuccessful);
                    }

                    return terminalClient.close(ServerError.CONNECTION_REFUSED_ERROR).then(Mono.empty());
                })
                .timeout(waitAfterAuthorizationIsPassed)
                .onErrorResume(TimeoutException.class, e -> terminalClient.close(ServerError.CONNECTION_REFUSED_ERROR).then(Mono.empty()));

    }


}
