package com.qnxy.terminal.client;

import com.qnxy.terminal.ClientManager;
import com.qnxy.terminal.ServerContext;
import com.qnxy.terminal.exceptions.TheRequestQueueHasReachedItsLimitException;
import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.ServerMessage;
import com.qnxy.terminal.message.client.AuthorizationApplication;
import com.qnxy.terminal.message.client.CompleteMessage;
import com.qnxy.terminal.message.client.ProactiveAsyncMessage;
import com.qnxy.terminal.message.client.ProactiveSyncMessage;
import com.qnxy.terminal.message.server.ServerError;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static com.qnxy.terminal.processor.ProactiveMessageProcessorFactory.findAsyncProcessor;
import static com.qnxy.terminal.processor.ProactiveMessageProcessorFactory.findSyncProcessor;


/**
 * @author Qnxy
 */
@Slf4j
public class ReactorNettyTerminalClient implements TerminalClient {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Sinks.Many<ServerMessage> requestSink = Sinks.unsafe().many().unicast().onBackpressureBuffer();
    private final Queue<Exchange> exchangeQueue = Queues.<Exchange>get(Queues.SMALL_BUFFER_SIZE).get();
    private final ReentrantLock lock = new ReentrantLock();

    private final Connection connection;
    private final ServerContext serverContext;
    private final ClientContext clientContext;
    private Disposable tcpDelayCloseDisposable;

    @Setter(AccessLevel.PRIVATE)
    private volatile Exchange exchange;

    public ReactorNettyTerminalClient(Connection connection, ServerContext serverContext) {
        this.connection = connection;
        this.serverContext = serverContext;

        this.clientContext = new ClientContext(new AtomicBoolean(false), serverContext);
        final ServerMessageEncoder serverMessageEncoder = new ServerMessageEncoder(connection.outbound().alloc());

        connection.onDispose(() -> ClientManager.removeClient(clientContext.getTerminalId()));
        connection.addHandlerLast(new LengthFieldBasedFrameDecoder(
                Short.MAX_VALUE - Short.BYTES,
                0,
                Short.BYTES,
                0,
                Short.BYTES
        ));

        this.clientDelayedClose();

        connection.inbound()
                .receive()
                .map(ClientMessageDecoder::decode)
                .doOnNext(it -> log.info("收到消息+ {}", it))
                .flatMap(this::dispatcherProcessor)
                .contextWrite(ctx -> ctx.put(ClientContext.class, this.clientContext))
                .subscribe();

        this.requestSink.asFlux()
                .doOnNext(it -> log.info("发送消息- {}", it))
                .map(serverMessageEncoder::encode)
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
        this.tcpDelayCloseDisposable = this.close(ServerError.MAXIMUM_AUTHORIZATION_WAIT_ERROR)
                .delaySubscription(this.serverContext.serverConfiguration().maximumAuthorizationWait())
                .subscribe();
    }

    private Mono<Void> dispatcherProcessor(ClientMessage message) {
        // 检查当前终端认证状态
        // 如果未认证则关闭连接
        if (!this.clientContext.getAuthorized().get() && !(message instanceof AuthorizationApplication)) {
            log.error("未认证并且当前消息不为认证消息: {}", message);
            return this.close(ServerError.CONNECTION_REFUSED_ERROR);
        }

        // 主动异步消息 如心跳等.
        if (message instanceof ProactiveAsyncMessage proactiveAsyncMessage) {
            return findAsyncProcessor(proactiveAsyncMessage.getClass())
                    .map(it -> it.handle(this, proactiveAsyncMessage))
                    .orElseGet(() -> this.close(ServerError.SERVER_EXCEPTION));
        }

        // 同步消息
        if (message instanceof ProactiveSyncMessage proactiveSyncMessage) {
            if (this.exchange == null) {
                return findSyncProcessor(proactiveSyncMessage.getClass())
                        .map(it -> it.handle(this, proactiveSyncMessage, newResponseMessageFlux()))
                        .orElseGet(() -> this.close(ServerError.SERVER_EXCEPTION));
            }

            log.debug("同步消息进行中, 当前消息已拒绝: {}", message);
            this.send(ServerError.SYNCHRONIZATION_TASK_IN_PROGRESS);
            return Mono.empty();
        }

        if (this.exchange == null && !this.nextExchangeQueueValue()) {
            // 收到错误消息
            return this.close(ServerError.UNEXPECTED_MESSAGE_ERROR);
        }

        if (this.exchange.emit(message)) {
            this.nextExchangeQueueValue();
        }

        return Mono.empty();
    }

    private Flux<ClientMessage> newResponseMessageFlux() {
        this.exchange = new Exchange();
        return Flux.create(sink -> exchange.sink = sink);
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
    public Mono<Void> close(ServerMessage message) {
        return Mono.defer(() -> {
            if (this.isClosed.compareAndSet(false, true)) {

                if (!this.connection.channel().isOpen()) {
                    this.connection.dispose();
                    return this.connection.onDispose();
                }

                this.cancelClientDelayedClose();
                this.send(message);
                this.connection.dispose();
                return this.connection.onDispose();
            }

            return Mono.empty();
        });
    }

    @Override
    public void registerReadIdle(Duration idle) {
        this.connection.onReadIdle(
                idle.toMillis(),
                () -> this.close(ServerError.HEARTBEAT_TIMEOUT_ERROR).subscribe()
        );
    }

    private boolean isConnected() {
        if (this.isClosed.get()) {
            return false;
        }

        return this.connection.channel().isOpen();
    }


    @Override
    public void cancelClientDelayedClose() {
        Optional.ofNullable(this.tcpDelayCloseDisposable)
                .filter(it -> !it.isDisposed())
                .ifPresent(Disposable::dispose);
    }

    @Override
    public void send(ServerMessage message) {
        this.requestSink.tryEmitNext(message);
    }

    @Override
    public Flux<ClientMessage> exchange(ServerMessage message) {
        return Flux.<ClientMessage>create(sink -> {
                    if (!this.isConnected()) {
                        sink.error(new RuntimeException("连接已关闭, 无法发送消息"));
                    }

                    try {
                        this.lock.lock();
                        if (this.exchangeQueue.isEmpty() && this.exchange == null) {
                            this.exchangeQueue.offer(new Exchange(sink));
                            this.requestSink.tryEmitNext(message);
                        } else {
                            if (!this.exchangeQueue.offer(new Exchange(sink, message))) {
                                sink.error(new TheRequestQueueHasReachedItsLimitException());
                            }
                        }
                    } catch (Exception e) {
                        sink.error(e);
                    } finally {
                        this.lock.unlock();
                    }
                })
                .timeout(this.clientContext.getSynchronousExecutionMaximumWaitTime());
    }


    private Mono<Void> resumeError(Throwable throwable) {

        return this.close(null);
    }


    @AllArgsConstructor
    @NoArgsConstructor
    private static class Exchange {
        private FluxSink<ClientMessage> sink;
        private ServerMessage nextMessage;

        private Exchange(FluxSink<ClientMessage> sink) {
            this.sink = sink;
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
