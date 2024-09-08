package com.qnxy.terminal.processor;

import com.qnxy.terminal.ClientMessageProcessor;
import com.qnxy.terminal.client.Client;
import com.qnxy.terminal.client.ClientContext;
import com.qnxy.terminal.message.client.Heartbeat;
import com.qnxy.terminal.message.server.SuccessfulMessage;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 心跳处理
 *
 * @author Qnxy
 */
@Slf4j
public class HeartbeatMessageProcessor implements ClientMessageProcessor<Heartbeat> {

    @Override
    public Mono<Void> handle(Heartbeat message, Client client) {
        return Mono.deferContextual(ctx -> {
            final Long terminalId = ctx.get(ClientContext.class).getTerminalId();
            log.debug("terminalId: {} -> {}", terminalId, message);

            client.send(SuccessfulMessage.INSTANCE);
            return Mono.empty();
        });
    }
}
