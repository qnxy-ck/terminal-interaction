package com.qnxy.terminal.client;

import com.qnxy.terminal.ClientManager;
import com.qnxy.terminal.ProactiveAsyncProcessor;
import com.qnxy.terminal.ProactiveSyncMessageProcessor;
import com.qnxy.terminal.ServerConfiguration;
import com.qnxy.terminal.api.TerminalExternalService;
import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.ClientMessageDecoder;
import com.qnxy.terminal.message.ServerMessage;
import com.qnxy.terminal.message.client.CompleteMessage;
import com.qnxy.terminal.message.client.ConnectAuthentication;
import com.qnxy.terminal.message.server.Quit;
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
import reactor.util.context.Context;

import java.time.Duration;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author Qnxy
 */
@Slf4j
public class ReactorNettyTerminalClient implements TerminalClient {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Sinks.Many<ServerMessage> requestSink = Sinks.many().unicast().onBackpressureBuffer();
    private final Queue<Exchange> exchangeQueue = Queues.<Exchange>get(Queues.SMALL_BUFFER_SIZE).get();
    private final ReentrantLock lock = new ReentrantLock();

    private final Connection connection;
    private final ServerConfiguration configuration;
    private final ClientContext clientContext;
    private final TerminalExternalService terminalExternalService;
    private Disposable tcpDelayCloseDisposable;

    @Setter(AccessLevel.PRIVATE)
    private volatile Exchange exchange;

    public ReactorNettyTerminalClient(Connection connection, ServerConfiguration configuration, TerminalExternalService terminalExternalService) {
        this.connection = connection;
        this.configuration = configuration;
        this.terminalExternalService = terminalExternalService;

        this.clientContext = new ClientContext(new AtomicBoolean(false));
        final MessageEncoder messageEncoder = new MessageEncoder(connection.outbound().alloc());

        connection.onDispose(() -> {
            ClientManager.removeClient(clientContext.getTerminalId());
            System.out.println("在线数量" + ClientManager.countClients());
        });
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
//                .doOnNext(it -> log.info("收到字节\n{}", ByteBufUtil.prettyHexDump(it)))
                .map(ClientMessageDecoder::decode)
                .doOnNext(it -> log.info("收到消息+ {}", it))
                .flatMap(this::dispatcherProcessor)
                .subscribe();

        this.requestSink.asFlux()
                .doOnNext(it -> log.info("发送消息- {}", it))
                .map(messageEncoder::encode)
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
                .delaySubscription(this.configuration.maximumAuthorizationWait())
                .subscribe();
    }

    private Mono<Void> dispatcherProcessor(ClientMessage message) {
        // 检查当前终端认证状态
        // 如果未认证则关闭连接
        if (!this.clientContext.getIsAuth().get() && !(message instanceof ConnectAuthentication)) {
            log.error("未认证并且当前消息不为认证消息: {}", message);
            return this.close(ServerError.CONNECTION_REFUSED_ERROR);
        }

        // 主动异步消息 如心跳等.
        if (message instanceof ProactiveAsyncProcessor proactiveAsyncProcessor) {
            return proactiveAsyncProcessor.handle(this)
                    .transform(this::proactiveProcessorContextWrite);
        }

        // 同步消息
        if (message instanceof ProactiveSyncMessageProcessor proactiveSyncMessageProcessor) {
            if (this.exchange == null) {
                return proactiveSyncMessageProcessor.handle(this, newFlux())
                        .transform(this::proactiveProcessorContextWrite);
            }

            log.debug("同步消息进行中, 当前消息已拒绝: {}", message);
            return Mono.fromRunnable(() -> this.send(ServerError.SYNCHRONIZATION_TASK_IN_PROGRESS));
        }

        if (this.exchange == null && !this.nextExchangeQueueValue()) {
            // 收到错误消息
            return Mono.fromRunnable(() -> this.send(ServerError.UNEXPECTED_MESSAGE_ERROR));
        }

        if (this.exchange.emit(message)) {
            this.nextExchangeQueueValue();
        }

        return Mono.empty();
    }

    private Flux<ClientMessage> newFlux() {
        this.exchange = new Exchange();
        return Flux.create(sink -> exchange.sink = sink);
    }

    private Mono<Void> proactiveProcessorContextWrite(Mono<Void> processor) {
        return processor.contextWrite(Context.of(
                ClientContext.class, this.clientContext,
                TerminalExternalService.class, this.terminalExternalService
        ));
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
                return Mono.just(Optional.ofNullable(message).orElse(Quit.INSTANCE))
                        .doOnNext(this::send)
                        .doOnSuccess(v -> this.connection.dispose())
                        .then(this.connection.onDispose());
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
                        if (this.exchangeQueue.isEmpty() && this.exchange == null) {
                            this.exchangeQueue.offer(new Exchange(sink));
                            this.requestSink.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
                        } else {
                            if (!this.exchangeQueue.offer(new Exchange(sink, message))) {
                                sink.error(new RuntimeException("请求队列已达到上限."));
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
