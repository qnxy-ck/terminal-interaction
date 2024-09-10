package com.qnxy.terminal.processor;

import com.qnxy.terminal.ProactiveSyncMessageProcessor;
import com.qnxy.terminal.client.TerminalClient;
import com.qnxy.terminal.message.ClientMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Qnxy
 */
public class SwipeCardProactiveSyncMessageProcessor implements ProactiveSyncMessageProcessor {
    

    @Override
    public Mono<Void> handle(TerminalClient terminalClient, Flux<ClientMessage> responseMessageFlux) {
        return null;
    }


}
