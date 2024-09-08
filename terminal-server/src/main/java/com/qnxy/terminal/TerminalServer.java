package com.qnxy.terminal;

import com.qnxy.terminal.server.NettyTcpApplicationEngine;

import java.time.Duration;

/**
 * @author Qnxy
 */
public class TerminalServer {

    public static void main(String[] args) {

        final ServerConfiguration configuration = new ServerConfiguration(
                9900,
                Duration.ofSeconds(10)
        );

        new NettyTcpApplicationEngine(configuration).start(true);

    }


}
