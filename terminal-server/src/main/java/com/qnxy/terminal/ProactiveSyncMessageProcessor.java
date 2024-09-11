package com.qnxy.terminal;

import com.qnxy.terminal.client.TerminalClient;
import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.client.ProactiveSyncMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 终端同步消息处理器
 *
 * @author Qnxy
 */
public interface ProactiveSyncMessageProcessor<CLIENT_MESSAGE extends ProactiveSyncMessage> {

    /**
     * 处理客户端主动发送的消息
     *
     * @param client              哪个客户端发来的
     * @param clientMessage       第一个消息是什么
     * @param responseMessageFlux 在该处理器中发送消息后 终端响应的后续消息
     */
    Mono<Void> handle(TerminalClient client, CLIENT_MESSAGE clientMessage, Flux<ClientMessage> responseMessageFlux);

}
