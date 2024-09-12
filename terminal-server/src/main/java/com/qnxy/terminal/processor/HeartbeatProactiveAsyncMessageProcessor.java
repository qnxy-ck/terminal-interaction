package com.qnxy.terminal.processor;

import com.qnxy.terminal.ProactiveAsyncMessageProcessor;
import com.qnxy.terminal.client.ClientContext;
import com.qnxy.terminal.client.TerminalClient;
import com.qnxy.terminal.message.client.Heartbeat;
import com.qnxy.terminal.message.server.Complete;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 心跳异步处理器
 * 收到终端心跳后返回 {@link Complete}
 * 
 * @author Qnxy
 */
@Slf4j
public class HeartbeatProactiveAsyncMessageProcessor implements ProactiveAsyncMessageProcessor<Heartbeat> {

    @Override
    public Mono<Void> handle(TerminalClient terminalClient, Heartbeat heartbeat) {
        return Mono.deferContextual(ctx -> {
            final Long terminalId = ctx.get(ClientContext.class).getTerminalId();
            log.debug("terminalId: {} -> {}", terminalId, heartbeat);

            terminalClient.send(Complete.INSTANCE);
            return Mono.empty();
        });
    }
}
