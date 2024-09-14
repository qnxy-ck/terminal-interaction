package com.qnxy.terminal;

import lombok.SneakyThrows;

import java.time.Duration;

/**
 * @author Qnxy
 */
public class TerminalServer {

    @SneakyThrows
    public static void main(String[] args) {

        final ServerConfiguration configuration = new ServerConfiguration(
                9900,
                Duration.ofSeconds(10),
                Duration.ofSeconds(5),
                3,
                Duration.ofSeconds(3)
        );

        new NettyTcpApplicationEngine(configuration)
                .start(true);

    }
}
