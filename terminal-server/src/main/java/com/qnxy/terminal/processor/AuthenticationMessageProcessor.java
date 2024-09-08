package com.qnxy.terminal.processor;

import com.qnxy.terminal.ClientManager;
import com.qnxy.terminal.ClientMessageProcessor;
import com.qnxy.terminal.api.AuthorizationServer;
import com.qnxy.terminal.client.Client;
import com.qnxy.terminal.client.ClientContext;
import com.qnxy.terminal.message.ServerErrorMessage;
import com.qnxy.terminal.message.client.Authentication;
import com.qnxy.terminal.message.server.AuthorizationSuccessfulMessage;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 连接时认证处理
 *
 * @author Qnxy
 */
@Slf4j
public class AuthenticationMessageProcessor implements ClientMessageProcessor<Authentication> {

    private AuthorizationServer authorizationServer;

    @Override
    public Mono<Void> handle(Authentication message, Client client) {
        return Mono.deferContextual(ctx -> {
            final ClientContext clientContext = ctx.get(ClientContext.class);
            if (clientContext.getIsAuth().get()) {
                return Mono.error(new RuntimeException("重复的认证消息, 当前已经认证"));
            }

            return authorize(message, client, clientContext);
        });
    }

    private Mono<Void> authorize(Authentication message, Client client, ClientContext clientContext) {
        // 调用外部服务接口, 获取授权信息
        return authorizationServer.authorize(message.imei())
                .flatMap(it -> {

                    // 判断当前连接是否已经存在
                    // 如果存在则直接关闭当前连接
                    if (ClientManager.findClient(it).isPresent()) {
                        log.error("该连接已存在无法建立, 当前连接已被关闭. IMEI: [{}] -- ID: [{}]", message.imei(), it);
                        return this.sendAndClose(client);
                    }

                    /*
                        取消未认证延迟关闭
                        设置当前客户端为已认证和相关信息
                     */
                    client.cancelTcpDelayClose();
                    clientContext.getIsAuth().set(true);
                    clientContext.setTerminalId(it)
                            .setImei(message.imei())
                    ;

                    // 添加到客户端管理器中 
                    ClientManager.addClient(it, client);
                    
                    // 发送认证成功的消息
                    final AuthorizationSuccessfulMessage authorizationSuccessfulMessage = new AuthorizationSuccessfulMessage(
                            (byte) 10,
                            (short) 180
                    );
                    client.send(authorizationSuccessfulMessage);
                    return Mono.empty();
                })
                .onErrorResume(e -> {
                    log.error("授权过程错误: {}", message.imei(), e);
                    return sendAndClose(client);
                });
    }


    private Mono<Void> sendAndClose(Client client) {
        client.send(ServerErrorMessage.CONNECTION_REFUSED_ERROR);
        return client.close();
    }
}
