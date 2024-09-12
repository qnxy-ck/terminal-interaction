package com.qnxy.terminal.client;

import com.qnxy.terminal.ServerConfiguration;
import com.qnxy.terminal.exceptions.TheRequestQueueHasReachedItsLimitException;
import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.ServerMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author Qnxy
 */
public interface TerminalClient {


    /**
     * 发送没有回应的消息
     *
     * @param message 消息内容
     */
    void send(ServerMessage message);

    /**
     * 发送带有回应的消息, 同步执行
     * <p>
     * 如果有同步消息正在执行 将会把此消息加入队列等待执行
     *
     * @param message 发送消息内容
     * @return 响应内容
     * @throws TheRequestQueueHasReachedItsLimitException 当队列已满时抛出该异常
     */
    Flux<ClientMessage> exchange(ServerMessage message);

    /**
     * 关闭当前终端
     */
    Mono<Void> close(ServerMessage message);

    /**
     * 注册当前客户端的心跳超时处理器
     * 每个终端心跳间隔不一样, 心跳间隔会和机器授权信息一起返回
     *
     * @param idle 超时时间
     */
    void registerReadIdle(Duration idle);

    /**
     * 取消当前终端未授权延时关闭
     * <p>
     * 当连接建立成功后
     * 如果在指定时间 {@link ServerConfiguration#maximumAuthorizationWait()}内未收到授权消息或者未成功授权则关闭连接
     */
    void cancelClientDelayedClose();


}
