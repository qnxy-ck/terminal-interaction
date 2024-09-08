package com.qnxy.terminal.processor;

import com.qnxy.terminal.ClientManager;
import com.qnxy.terminal.ClientMessageProcessor;
import com.qnxy.terminal.api.TerminalExternalService;
import com.qnxy.terminal.api.data.AuthorizationInfo;
import com.qnxy.terminal.client.Client;
import com.qnxy.terminal.client.ClientContext;
import com.qnxy.terminal.message.ServerErrorMessage;
import com.qnxy.terminal.message.client.ConnectAuthentication;
import com.qnxy.terminal.message.server.AuthorizationSuccessful;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 连接时认证处理
 *
 * @author Qnxy
 */
@Slf4j
public class AuthenticationMessageProcessor implements ClientMessageProcessor<ConnectAuthentication> {


    @Override
    public Mono<Void> handle(ConnectAuthentication message, Client client) {
        return Mono.deferContextual(ctx -> {
            final ClientContext clientContext = ctx.get(ClientContext.class);
            if (clientContext.getIsAuth().get()) {
                return Mono.error(new RuntimeException("重复的认证消息, 当前已经认证"));
            }

            return ctx.get(TerminalExternalService.class)
                    .authorize(message.imei())
                    .onErrorResume(e -> {
                        log.error("授权过程错误: {}", message.imei(), e);
                        return sendAndClose(client).then(Mono.empty());
                    })
                    .flatMap(it -> handler(message.imei(), client, it, clientContext));
        });
    }


    private Mono<Void> handler(String imei, Client client, AuthorizationInfo authorizationInfo, ClientContext clientContext) {
        // 判断当前连接是否已经存在
        // 如果存在则直接关闭当前连接
        final Long terminalId = authorizationInfo.getTerminalId();
        if (ClientManager.findClient(terminalId).isPresent()) {
            log.error("该连接已存在无法建立, 当前连接已被关闭. IMEI: [{}] -- ID: [{}]", imei, terminalId);
            return this.sendAndClose(client);
        }


        // 取消未认证延迟关闭
        // 设置当前客户端为已认证和相关信息
        client.cancelTcpDelayClose();
        clientContext.getIsAuth().set(true);
        clientContext.setTerminalId(terminalId)
                .setImei(imei)
                .setMaxWaitMoveOutGoodsTime(authorizationInfo.getTerminalWaitMaxMoveOutGoodsTime())
        ;

        // 心跳超时时间为每次心跳间隔的三倍
        // 超出该时间将被断开连接
        final Duration readIdleDuration = Duration.ofMillis(authorizationInfo.getTerminalHeartbeatInterval().toMillis() * 3);
        client.registerReadIdle(readIdleDuration);

        // 添加到客户端管理器中 
        ClientManager.addClient(terminalId, client);

        // 发送认证成功的消息
        client.send(new AuthorizationSuccessful(authorizationInfo.getTerminalHeartbeatInterval()));
        return Mono.empty();
    }


    private Mono<Void> sendAndClose(Client client) {
        client.send(ServerErrorMessage.CONNECTION_REFUSED_ERROR);
        return client.close();
    }

}
