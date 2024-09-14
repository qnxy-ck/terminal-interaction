package com.qnxy.springbootapp;

import com.qnxy.terminal.NettyTcpApplicationEngine;
import com.qnxy.terminal.ServerConfiguration;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Duration;

@SpringBootApplication
public class SpringBootAppApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAppApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        final ServerConfiguration configuration = new ServerConfiguration(
                9900,
                Duration.ofSeconds(10),
                Duration.ofSeconds(5),
                3,
                Duration.ofSeconds(3)
        );

        new NettyTcpApplicationEngine(configuration).start(false);
    }
}
