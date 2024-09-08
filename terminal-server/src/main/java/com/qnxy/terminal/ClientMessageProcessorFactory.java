package com.qnxy.terminal;

import com.qnxy.terminal.client.Client;
import com.qnxy.terminal.message.client.ConnectAuthentication;
import com.qnxy.terminal.message.client.Heartbeat;
import com.qnxy.terminal.message.client.ProactiveMessages;
import com.qnxy.terminal.message.client.SwipeCard;
import com.qnxy.terminal.processor.AuthenticationMessageProcessor;
import com.qnxy.terminal.processor.HeartbeatMessageProcessor;
import com.qnxy.terminal.processor.SwipeCardMessageProcessor;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Qnxy
 */
public final class ClientMessageProcessorFactory {

    private static final Map<Class<? extends ProactiveMessages>, ClientMessageProcessor<? extends ProactiveMessages>> processors = new ConcurrentHashMap<>();

    static {
        processors.put(Heartbeat.class, new HeartbeatMessageProcessor());
        processors.put(ConnectAuthentication.class, new AuthenticationMessageProcessor());
        processors.put(SwipeCard.class, new SwipeCardMessageProcessor());
    }


    @SuppressWarnings("unchecked")
    public static <M extends ProactiveMessages> Mono<Void> doHandle(M message, Client client) {
        return Optional.ofNullable(processors.get(message.getClass()))
                .map(it -> ((ClientMessageProcessor<M>) it).handle(message, client))
                .orElse(Mono.error(new RuntimeException("不存在该消息类型的处理器: " + message.getClass())));
    }

}
