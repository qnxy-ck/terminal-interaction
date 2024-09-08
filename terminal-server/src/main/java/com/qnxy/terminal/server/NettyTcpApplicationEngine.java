package com.qnxy.terminal.server;

import com.qnxy.terminal.ApplicationEngine;
import com.qnxy.terminal.ServerConfiguration;
import com.qnxy.terminal.api.TerminalExternalService;
import com.qnxy.terminal.client.TerminalClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Sinks;
import reactor.netty.tcp.TcpServer;

/**
 * @author Qnxy
 */
@RequiredArgsConstructor
public class NettyTcpApplicationEngine implements ApplicationEngine {

    private final ServerConfiguration configuration;
    private final Sinks.Empty<Void> shutdownSink = Sinks.empty();


    @Override
    public void start(boolean wait) {

        TcpServer.create()
                .doOnConnection(it -> new TerminalClient(it, configuration, TerminalExternalService.INSTANCE))
                .port(configuration.port())
                .bind()
                .subscribe(it -> Runtime.getRuntime().addShutdownHook(new Thread(it::dispose)));

        if (wait) {
            shutdownSink.asMono().block();
        }

    }

    @Override
    public void stop() {
        shutdownSink.tryEmitEmpty();
    }

}
