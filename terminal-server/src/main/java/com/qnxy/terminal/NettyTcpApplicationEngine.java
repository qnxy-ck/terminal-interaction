package com.qnxy.terminal;

import com.qnxy.terminal.api.TerminalExternalService;
import com.qnxy.terminal.client.ReactorNettyTerminalClient;
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
                .doOnConnection(it -> new ReactorNettyTerminalClient(it, configuration, TerminalExternalService.INSTANCE))
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
