package com.qnxy.terminal;

import com.qnxy.terminal.client.Client;
import com.qnxy.terminal.message.client.ProactiveMessages;
import reactor.core.publisher.Mono;

/**
 * @author Qnxy
 */
@FunctionalInterface
public interface ClientMessageProcessor<MESSAGE extends ProactiveMessages> {

    Mono<Void> handle(MESSAGE message, Client client);

}
