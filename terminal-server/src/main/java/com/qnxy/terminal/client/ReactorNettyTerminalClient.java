package com.qnxy.terminal.client;

import com.qnxy.terminal.ClientManager;
import com.qnxy.terminal.ServerConfiguration;
import com.qnxy.terminal.api.TerminalExternalService;
import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.ClientMessageDecoder;
import com.qnxy.terminal.message.ServerErrorMessage;
import com.qnxy.terminal.message.ServerMessage;
import com.qnxy.terminal.message.client.CompleteMessage;
import com.qnxy.terminal.message.client.ConnectAuthentication;
import com.qnxy.terminal.message.client.ProactiveMessages;
import com.qnxy.terminal.message.client.ProactiveSyncMessages;
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
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Qnxy
 */
@Slf4j
public class ReactorNettyTerminalClient implements TerminalClient {

    private final Connection connection;
    private final ServerConfiguration configuration;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final ReentrantLock lock = new ReentrantLock();
    private final Sinks.Many<ServerMessage> requestSink = Sinks.many().unicast().onBackpressureBuffer();
    private final Queue<Exchange> exchangeQueue = Queues.<Exchange>get(Queues.SMALL_BUFFER_SIZE).get();
    private final ClientContext clientContext;
    private final TerminalExternalService terminalExternalService;


    private Disposable tcpDelayCloseDisposable;
    private volatile Exchange exchange;

    public ReactorNettyTerminalClient(Connection connection, ServerConfiguration configuration, TerminalExternalService terminalExternalService) {
        this.connection = connection;
        this.configuration = configuration;
        this.terminalExternalService = terminalExternalService;

        this.clientContext = new ClientContext(new AtomicBoolean(false));

        connection.onDispose(() -> ClientManager.removeClient(clientContext.getTerminalId()));
        connection.addHandlerLast(new LengthFieldBasedFrameDecoder(Short.MAX_VALUE, 0, Short.BYTES, 0, 3));
        this.clientDelayedClose();

        connection.inbound()
                .receive()
                .map(ClientMessageDecoder::decode)
                .flatMap(this::dispatcherProcessor)
                .subscribe();

        this.requestSink.asFlux()
                .map(it -> it.encode(connection.outbound().alloc()))
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
        if (!this.clientContext.getIsAuth().get() && !(message instanceof ConnectAuthentication)) {
            // 未认证并且当前消息不为认证消息
            log.error("未认证并且当前消息不为认证消息: {}", message);
            this.send(ServerErrorMessage.CONNECTION_REFUSED_ERROR);
            return this.close();
        }

        if (message instanceof ProactiveMessages proactiveMessages) {
            // 如果是终端主动消息则选择对应处理器进行处理

            if (this.exchange != null) {
                // 如果上个消息还没有处理完毕, 又收到下一个终端主动消息
                // 则认为收到不符合预期消息, 将关闭当前客户端
                log.error("收到不符合预期的消息: {}", proactiveMessages);
                this.send(ServerErrorMessage.UNEXPECTED_MESSAGE_ERROR);
                return this.close();
            }


            if (message instanceof ProactiveSyncMessages) {
                // 如果为同步消息则进行加锁
                // 主要防止在收到终端消息后 被服务器抢先执行其他流程
                return Mono.<Mono<Void>>create(sink -> this.withLock(
                                () -> sink.success(this.proactiveMessagesHandler(proactiveMessages)),
                                sink::error
                        ))
                        .flatMap(Function.identity());
            }

            return this.proactiveMessagesHandler(proactiveMessages);

        }

        if (this.exchange == null && !this.nextExchangeQueueValue()) {
            log.error("收到不符合预期的消息: {}", message);
            this.send(ServerErrorMessage.UNEXPECTED_MESSAGE_ERROR);
            return this.close();
        }

        if (this.exchange.emit(message)) {
            this.nextExchangeQueueValue();
        }

        return Mono.empty();
    }

    private Mono<Void> proactiveMessagesHandler(ProactiveMessages proactiveMessages) {
        return proactiveMessages.handle(this)
                .contextWrite(ctx -> ctx.putAllMap(Map.of(
                        ClientContext.class, this.clientContext,
                        TerminalExternalService.class, this.terminalExternalService
                )));
    }

    private boolean nextExchangeQueueValue() {
        this.exchange = this.exchangeQueue.poll();
        if (this.exchange == null) {
            return false;
        }

        Optional.ofNullable(this.exchange.nextMessage)
                .ifPresent(it -> this.requestSink.emitNext(it, Sinks.EmitFailureHandler.FAIL_FAST));
        return true;
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

    private boolean isConnected() {
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
    public void cancelClientDelayedClose() {
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
                    this.withLock(
                            () -> {
                                // 如果处理队列为空则之间发送消息
                                // 否则则将消息添加到待发送队列中, 等上个消息处理完成之后才会发送下个消息
                                if (this.exchangeQueue.isEmpty()) {
                                    this.exchangeQueue.offer(new Exchange(sink));
                                    this.requestSink.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
                                } else {

                                    if (!this.exchangeQueue.offer(new Exchange(sink, message))) {
                                        sink.error(new RuntimeException("请求队列已达到上限."));
                                    }
                                }
                            },
                            sink::error
                    );
                })
                .timeout(this.clientContext.getSynchronousExecutionMaximumWaitTime());
    }


    private Mono<Void> resumeError(Throwable throwable) {


        return this.close();
    }

    private void withLock(Runnable runnable, Consumer<Exception> error) {
        try {
            this.lock.lock();
            runnable.run();
        } catch (Exception e) {
            error.accept(e);
        } finally {
            this.lock.unlock();
        }
    }


    record Exchange(
            FluxSink<ClientMessage> sink,
            ServerMessage nextMessage
    ) {

        Exchange(FluxSink<ClientMessage> sink) {
            this(sink, null);
        }

        boolean emit(ClientMessage message) {
            if (this.sink.isCancelled()) {
                log.error("调用者已关闭: {}", message);

                return true;
            }

            sink.next(message);
            if (message instanceof CompleteMessage) {
                sink.complete();
                return true;
            }
            return false;
        }
    }

}
