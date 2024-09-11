package com.qnxy.terminal.processor;

import com.qnxy.terminal.ClientManager;
import com.qnxy.terminal.ProactiveSyncMessageProcessor;
import com.qnxy.terminal.client.ClientContext;
import com.qnxy.terminal.client.TerminalClient;
import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.client.ConnectAuthentication;
import com.qnxy.terminal.message.client.SetupSuccessful;
import com.qnxy.terminal.message.server.AuthorizationPassed;
import com.qnxy.terminal.message.server.ServerError;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author Qnxy
 */
@Slf4j
public class ConnectAuthenticationProactiveSyncMessageProcessor implements ProactiveSyncMessageProcessor<ConnectAuthentication> {

    @Override
    public Mono<Void> handle(TerminalClient client, ConnectAuthentication connectAuthentication, Flux<ClientMessage> responseMessageFlux) {
        return Mono.deferContextual(ctx -> {
            final ClientContext clientContext = ctx.get(ClientContext.class);

            if (clientContext.getAuthorized().compareAndSet(false, true)) {
                client.cancelClientDelayedClose();
                return doAuthorize(client, connectAuthentication, responseMessageFlux, clientContext);
            }

            log.error("重复的认证消息, 当前已经认证: {} -> {}", clientContext.getTerminalId(), connectAuthentication);

            // 重复认证, 直接关闭该终端连接 
            return client.close(ServerError.UNEXPECTED_MESSAGE_ERROR);
        });

    }

    private Mono<Void> doAuthorize(TerminalClient terminalClient, ConnectAuthentication connectAuthentication, Flux<ClientMessage> responseMessageFlux, ClientContext clientContext) {
        return clientContext.getServerContext()
                .terminalExternalService()
                .authorize(connectAuthentication.imei())
                .onErrorResume(e -> {
                    log.error("授权过程错误: {}", connectAuthentication.imei(), e);
                    return terminalClient.close(ServerError.CONNECTION_REFUSED_ERROR).then(Mono.empty());
                })
                .flatMap(it -> {
                    // 判断当前连接是否已经存在
                    // 如果存在则直接关闭当前连接
                    final Long terminalId = it.getTerminalId();
                    if (ClientManager.findClient(terminalId).isPresent()) {
                        log.error("该连接已存在无法建立, 当前连接已被关闭. IMEI: [{}] -- ID: [{}]", connectAuthentication.imei(), terminalId);
                        return terminalClient.close(ServerError.CONNECTION_REFUSED_ERROR);
                    }

                    // 发送认证成功的消息
                    terminalClient.send(new AuthorizationPassed(it.getTerminalHeartbeatInterval()));
                    
                    return this.waitTerminalSuccessResp(responseMessageFlux, terminalClient)
                            .doOnNext(successful -> {
                                clientContext.setAuthInfo(
                                        terminalId,
                                        connectAuthentication.imei(),
                                        it.getSynchronousExecutionMaximumWaitTime()
                                );

                                // 心跳超时时间为每次心跳间隔的三倍
                                // 超出该时间将被断开连接
                                final Duration readIdleDuration = Duration.ofMillis(it.getTerminalHeartbeatInterval().toMillis() * 3);
                                terminalClient.registerReadIdle(readIdleDuration);

                                // 添加到客户端管理器中 
                                ClientManager.addClient(terminalId, terminalClient);
                            });
                })
                .then();
    }


    public Mono<SetupSuccessful> waitTerminalSuccessResp(Flux<ClientMessage> responseMessageFlux, TerminalClient terminalClient) {
        return responseMessageFlux.last()
                .flatMap(it -> {
                    if (it instanceof SetupSuccessful setupSuccessful) {
                        return Mono.just(setupSuccessful);
                    }

                    return terminalClient.close(ServerError.CONNECTION_REFUSED_ERROR).then(Mono.empty());
                });

    }


}
