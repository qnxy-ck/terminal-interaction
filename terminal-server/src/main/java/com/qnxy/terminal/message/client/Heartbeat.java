package com.qnxy.terminal.message.client;

import com.qnxy.terminal.client.ClientContext;
import com.qnxy.terminal.client.TerminalClient;
import com.qnxy.terminal.message.server.Successful;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * @author Qnxy
 */
@ToString
@Slf4j
public final class Heartbeat implements ProactiveMessages {

    public static final Heartbeat INSTANCE = new Heartbeat();

    private Heartbeat() {
    }

    @Override
    public Mono<Void> handle(TerminalClient terminalClient) {
        return Mono.deferContextual(ctx -> {
            final Long terminalId = ctx.get(ClientContext.class).getTerminalId();
            log.debug("terminalId: {} -> {}", terminalId, this);

            terminalClient.send(Successful.INSTANCE);
            return Mono.empty();
        });
    }
}
