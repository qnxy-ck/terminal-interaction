package com.qnxy.terminal.message.client;

import com.qnxy.terminal.ClientManager;
import com.qnxy.terminal.IllegalEncodingException;
import com.qnxy.terminal.ProactiveAsyncProcessor;
import com.qnxy.terminal.ProtocolVersion;
import com.qnxy.terminal.api.TerminalExternalService;
import com.qnxy.terminal.api.data.AuthorizationInfo;
import com.qnxy.terminal.client.ClientContext;
import com.qnxy.terminal.client.TerminalClient;
import com.qnxy.terminal.message.server.AuthorizationSuccessful;
import com.qnxy.terminal.message.server.ServerError;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 通讯认证
 *
 * @author Qnxy
 */
@Slf4j
public record ConnectAuthentication(
        ProtocolVersion protocolVersion,
        String imei
) implements ProactiveAsyncProcessor, CompleteMessage {

    public static ConnectAuthentication decode(ByteBuf byteBuf) {
        final byte versionNum = byteBuf.readByte();
        final ProtocolVersion protocolVersion = ProtocolVersion.valueOf(versionNum)
                .orElseThrow(() -> new IllegalEncodingException("未知协议版本: " + versionNum));

        final CharSequence imei = byteBuf.readCharSequence(byteBuf.readableBytes(), StandardCharsets.UTF_8);
        return new ConnectAuthentication(protocolVersion, imei.toString());
    }


    @Override
    public Mono<Void> handle(TerminalClient terminalClient) {
        return Mono.deferContextual(ctx -> {
            final ClientContext clientContext = ctx.get(ClientContext.class);
            if (clientContext.getIsAuth().get()) {
                return Mono.error(new RuntimeException("重复的认证消息, 当前已经认证"));
            }

            return ctx.get(TerminalExternalService.class).authorize(this.imei)
                    .onErrorResume(e -> {
                        log.error("授权过程错误: {}", this.imei, e);
                        return terminalClient.close(ServerError.CONNECTION_REFUSED_ERROR).then(Mono.empty());
                    })
                    .flatMap(it -> resultHandler(terminalClient, it, clientContext));
        });
    }

    private Mono<Void> resultHandler(TerminalClient terminalClient, AuthorizationInfo authorizationInfo, ClientContext clientContext) {
        // 判断当前连接是否已经存在
        // 如果存在则直接关闭当前连接
        final Long terminalId = authorizationInfo.getTerminalId();
        if (ClientManager.findClient(terminalId).isPresent()) {
            log.error("该连接已存在无法建立, 当前连接已被关闭. IMEI: [{}] -- ID: [{}]", this.imei, terminalId);
            return terminalClient.close(ServerError.CONNECTION_REFUSED_ERROR);
        }


        // 取消未认证延迟关闭
        // 设置当前客户端为已认证和相关信息
        terminalClient.cancelClientDelayedClose();
        clientContext.getIsAuth().set(true);
        clientContext.setTerminalId(terminalId)
                .setImei(this.imei)
                .setSynchronousExecutionMaximumWaitTime(authorizationInfo.getSynchronousExecutionMaximumWaitTime())
        ;

        // 心跳超时时间为每次心跳间隔的三倍
        // 超出该时间将被断开连接
        final Duration readIdleDuration = Duration.ofMillis(authorizationInfo.getTerminalHeartbeatInterval().toMillis() * 3);
        terminalClient.registerReadIdle(readIdleDuration);

        // 添加到客户端管理器中 
        ClientManager.addClient(terminalId, terminalClient);
        log.info("当前在线数量: {}", ClientManager.countClients());

        // 发送认证成功的消息
        terminalClient.send(new AuthorizationSuccessful(authorizationInfo.getTerminalHeartbeatInterval()));
        return Mono.empty();
    }

}
