package com.qnxy.terminal.message.client;

import com.qnxy.terminal.exceptions.TheRequestQueueHasReachedItsLimitException;
import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.server.ServerError;

/**
 * 终端完成消息类型标志接口
 * <p>
 * 在同步消息上时是必要的, 当终端发送该类型消息后才会终止本次同步执行 否则后续消息将一直无法获取到执行权
 * <p>
 * 在同步消息执行时:
 * <p>
 * 1. 终端主动消息将直接忽略执行, 直接返回 {@link ServerError#SYNCHRONIZATION_TASK_IN_PROGRESS}
 * <p>
 * 2. 服务器主动消息将进入队列等候, 如果队列已满则直接抛出异常 {@link TheRequestQueueHasReachedItsLimitException}
 *
 * @author Qnxy
 */
public interface CompleteMessage extends ClientMessage {

}
