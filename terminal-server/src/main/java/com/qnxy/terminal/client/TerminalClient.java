package com.qnxy.terminal.client;

import com.qnxy.terminal.ClientManager;
import com.qnxy.terminal.ClientMessageProcessorFactory;
import com.qnxy.terminal.ServerConfiguration;
import com.qnxy.terminal.UnauthenticatedException;
import com.qnxy.terminal.api.TerminalExternalService;
import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.ClientMessageDecoder;
import com.qnxy.terminal.message.ServerErrorMessage;
import com.qnxy.terminal.message.ServerMessage;
import com.qnxy.terminal.message.client.CompleteMessage;
import com.qnxy.terminal.message.client.ConnectAuthentication;
import com.qnxy.terminal.message.client.ProactiveMessages;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Qnxy
 */
@Slf4j
public class TerminalClient implements Client {

    private final Connection connection;
    private final ServerConfiguration configuration;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final AtomicBoolean isAuth = new AtomicBoolean(false);
    private final ReentrantLock lock = new ReentrantLock();


    private final Sinks.Many<ServerMessage> requestSink = Sinks.many().unicast().onBackpressureBuffer();
    private final Sinks.Many<ClientMessage> responseSink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
    private final Queue<FluxSink<ClientMessage>> exchangeQueue = Queues.<FluxSink<ClientMessage>>get(Queues.SMALL_BUFFER_SIZE).get();
    private final ByteBufAllocator byteBufAllocator;
    private final ClientContext clientContext;
    private final TerminalExternalService terminalExternalService;


    private Disposable tcpDelayCloseDisposable;
    private volatile FluxSink<ClientMessage> sink;


    public TerminalClient(Connection connection, ServerConfiguration configuration, TerminalExternalService terminalExternalService) {
        this.connection = connection;
        this.configuration = configuration;
        this.terminalExternalService = terminalExternalService;
        this.byteBufAllocator = connection.outbound().alloc();
        this.clientContext = new ClientContext(isAuth);

        connection.onDispose(() -> ClientManager.removeClient(clientContext.getTerminalId()));
        connection.addHandlerLast(new LengthFieldBasedFrameDecoder(Short.MAX_VALUE, 0, Short.BYTES, 0, 3));
        this.clientDelayedClose();

        connection.inbound()
                .receive()
                .map(ClientMessageDecoder::decode)
                .flatMap(this::dispatcherProcessor)
                .subscribe();

        this.requestSink.asFlux()
                .map(it -> it.encode(this.byteBufAllocator))
                .flatMap(it -> this.connection.outbound().send(it), 1)
                .onErrorResume(this::resumeError)
                .doAfterTerminate(() -> {
                })
                .subscribe();

    }


    /**
     * 客户端延迟关闭
     * <p>
     * 在达到配置最大授权时间后 仍未被取消则关闭当前客户端
     */
    private void clientDelayedClose() {
        this.tcpDelayCloseDisposable = Mono.defer(() -> {
                    this.send(ServerErrorMessage.MAXIMUM_AUTHORIZATION_WAIT_ERROR);
                    return this.close();
                })
                .delaySubscription(this.configuration.maximumAuthorizationWait())
                .subscribe();
    }


    private Mono<Void> dispatcherProcessor(ClientMessage message) {
        if (!isAuth.get() && !(message instanceof ConnectAuthentication)) {
            // 未认证并且当前消息不为认证消息
            // 抛出未认证异常
            return Mono.error(new UnauthenticatedException());
        }

        if (message instanceof ProactiveMessages proactiveMessages) {
            // 如果是终端主动消息则选择对应处理器进行处理

            if (this.sink != null) {
                return Mono.error(new RuntimeException("收到不符合预期的消息: " + proactiveMessages));
            }

            return ClientMessageProcessorFactory.doHandle(proactiveMessages, this)
                    .contextWrite(ctx -> ctx.putAllMap(Map.of(
                            ClientContext.class, this.clientContext,
                            TerminalExternalService.class, this.terminalExternalService
                    )));
        }

        if (this.sink == null && !this.nextExchangeQueueValue()) {
            return Mono.error(new RuntimeException("收到错误消息: " + message));
        }

        if (this.sink.isCancelled()) {
            this.sink = null;
            return Mono.error(new RuntimeException("调用者已关闭: " + message));
        }

        this.sink.next(message);

        if (message instanceof CompleteMessage) {
            this.sink.complete();
            this.nextExchangeQueueValue();
        }

        return Mono.error(new RuntimeException("收到意外的消息: " + message));
    }

    public boolean nextExchangeQueueValue() {
        this.sink = this.exchangeQueue.poll();
        return this.sink != null;
    }

    @Override
    public Mono<Void> close() {
        return Mono.defer(() -> {
            if (this.isClosed.compareAndSet(false, true)) {

                if (!this.connection.channel().isOpen()) {
                    return this.closeConnection();
                }

                return this.closeConnection();
            }

            log.info("该客户端已经被关闭.");
            return Mono.empty();
        });
    }

    @Override
    public void registerReadIdle(Duration idle) {
        this.connection.onReadIdle(idle.toMillis(), () -> {
            this.send(ServerErrorMessage.HEARTBEAT_TIMEOUT_ERROR);
            this.close().subscribe();
        });
    }

    @Override
    public boolean isConnected() {
        if (this.isClosed.get()) {
            return false;
        }

        return this.connection.channel().isOpen();
    }

    private Mono<? extends Void> closeConnection() {
        this.connection.dispose();
        return this.connection.onDispose();
    }

    @Override
    public void cancelTcpDelayClose() {
        Optional.ofNullable(this.tcpDelayCloseDisposable)
                .ifPresent(Disposable::dispose);
    }

    @Override
    public void send(ServerMessage message) {
        this.requestSink.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    @Override
    public Flux<ClientMessage> exchange(ServerMessage message) {
        return Flux.<ClientMessage>create(sink -> {
                    if (!this.isConnected()) {
                        sink.error(new RuntimeException("连接已关闭, 无法发送消息"));
                    }

                    try {
                        this.lock.lock();
                        if (this.exchangeQueue.offer(sink)) {
                            this.requestSink.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
                        } else {
                            sink.error(new RuntimeException("请求队列已达到上限."));
                        }
                    } catch (Exception e) {
                        sink.error(e);
                    } finally {
                        this.lock.unlock();
                    }
                })
                .timeout(this.clientContext.getMaxWaitMoveOutGoodsTime());
    }


    private Mono<Void> resumeError(Throwable throwable) {


        return this.close();
    }

}
