package com.qnxy.terminal.message.client;

import com.qnxy.terminal.ProactiveAsyncMessageProcessor;
import com.qnxy.terminal.ProactiveSyncMessageProcessor;
import com.qnxy.terminal.message.ClientMessage;

/**
 * 终端主动异步消息类型标志
 * <p>
 * 终端消息实现该接口后, 当终端主动发出消息时将进行异步响应
 * 实现该类型的消息必须实现 {@link ProactiveAsyncMessageProcessor} 接口
 * 在收到消息后会派发消息到该处理器进行执行
 * <p>
 * <p>
 * 相反当终端消息实现 {@link ProactiveSyncMessage} 接口后, 收到终端消息后将进行排队执行
 * 详细查看 {@link ProactiveSyncMessage} {@link ProactiveSyncMessageProcessor}
 *
 * @author Qnxy
 */
public interface ProactiveAsyncMessage extends ClientMessage {
}
