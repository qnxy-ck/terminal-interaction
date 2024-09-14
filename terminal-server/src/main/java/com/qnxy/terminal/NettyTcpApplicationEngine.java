package com.qnxy.terminal;

import com.qnxy.terminal.client.ReactorNettyTerminalClient;
import com.qnxy.terminal.external.TerminalExternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

import java.util.Optional;

/**
 * @author Qnxy
 */
@Slf4j
@RequiredArgsConstructor
public class NettyTcpApplicationEngine implements ApplicationEngine {

    private final ServerConfiguration configuration;
    private final Sinks.Empty<Void> shutdownSink = Sinks.empty();

    private DisposableServer disposableServer;

    @Override
    public void start(boolean wait) {
        final ServerContext serverContext = new ServerContext(
                TerminalExternalService.INSTANCE,
                configuration
        );

        TcpServer.create()
                .doOnConnection(it -> new ReactorNettyTerminalClient(it, serverContext))
                .port(configuration.port())
                .bind()
                .doOnNext(this::setDisposableServer)
                .flatMap(it -> wait ? shutdownSink.asMono() : Mono.empty())
                .block();

    }

    private void setDisposableServer(DisposableServer disposableServer) {
        this.disposableServer = disposableServer;
        log.info("服务器已启动, 监听端口: {}", configuration.port());
        disposableServer.onDispose(() -> log.info("服务器已关闭"));
        Runtime.getRuntime().addShutdownHook(new Thread(disposableServer::dispose));
    }

    @Override
    public void stop() {
        Optional.ofNullable(this.disposableServer)
                .ifPresent(DisposableServer::dispose);
        shutdownSink.tryEmitEmpty();
    }

}
