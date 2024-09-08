package com.qnxy.terminal.client;

import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.ServerMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author Qnxy
 */
public interface Client {

    Mono<Void> close();

    void registerReadIdle(Duration idle);

    boolean isConnected();

    void cancelTcpDelayClose();

    void send(ServerMessage message);

    Flux<ClientMessage> exchange(ServerMessage message);


}
