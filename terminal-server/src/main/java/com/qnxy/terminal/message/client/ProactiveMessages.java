package com.qnxy.terminal.message.client;

import com.qnxy.terminal.client.TerminalClient;
import com.qnxy.terminal.message.ClientMessage;
import reactor.core.publisher.Mono;

/**
 * 终端主动发送消息
 * <p>
 * 该类型消息必须存在对应的处理器
 *
 * @author Qnxy
 */
public interface ProactiveMessages extends ClientMessage {

    Mono<Void> handle(TerminalClient terminalClient);

}
