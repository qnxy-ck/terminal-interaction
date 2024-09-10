package com.qnxy.terminal;

import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.ServerMessage;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpClient;

/**
 * @author Qnxy
 */
public interface TerminalClient {


    static Mono<TerminalClient> connect(String host, int port) {
        return TcpClient.create()
                .host(host)
                .port(port)
                .connect()
                .flatMap(it -> Mono.just(new ReactorNettyTerminalClient(it)));
    }

    Mono<Void> close();

    Mono<ServerMessage> exchange(ClientMessage message);

    void send(ClientMessage message);


}
