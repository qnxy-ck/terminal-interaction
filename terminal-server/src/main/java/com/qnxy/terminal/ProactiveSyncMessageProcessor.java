package com.qnxy.terminal;

import com.qnxy.terminal.client.TerminalClient;
import com.qnxy.terminal.message.ClientMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 终端同步消息处理器
 *
 * @author Qnxy
 */
public interface ProactiveSyncMessageProcessor {

    Mono<Void> handle(TerminalClient terminalClient, Flux<ClientMessage> responseMessageFlux);

}
