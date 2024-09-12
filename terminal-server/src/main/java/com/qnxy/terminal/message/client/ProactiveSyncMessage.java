package com.qnxy.terminal.message.client;

import com.qnxy.terminal.ProactiveAsyncMessageProcessor;
import com.qnxy.terminal.ProactiveSyncMessageProcessor;
import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.server.ServerError;

/**
 * 终端主动同步消息类型标志接口
 * <p>
 * 终端消息实现该接口后, 当终端主动发出消息时将进行同步执行
 * 实现该接口的消息同时也需要实现 {@link ProactiveSyncMessageProcessor} 处理器, 当收到该消息时将派发到该处理器执行
 * 直至处理完成 参考 {@link CompleteMessage}
 *
 * <p>
 * 如果此时正在处理同步消息, 终端继续发送实现 {@link ProactiveSyncMessage} 类型的消息将拒绝处理
 * 返回 {@link ServerError#SYNCHRONIZATION_TASK_IN_PROGRESS}
 * <p>
 * 如需异步消息参考 {@link ProactiveAsyncMessage} {@link ProactiveAsyncMessageProcessor}
 *
 * @author Qnxy
 */
public interface ProactiveSyncMessage extends ClientMessage {
}
