package com.qnxy.terminal;

import com.qnxy.terminal.client.TerminalClient;
import reactor.core.publisher.Mono;

/**
 * 处理终端主动发送异步消息
 * 该处理器无法处理在此处理器中发送消息后续的响应 因为它是异步的
 * 用于处理类上心跳等
 *
 * @author Qnxy
 */
public interface ProactiveAsyncProcessor {

    Mono<Void> handle(TerminalClient terminalClient);

}
