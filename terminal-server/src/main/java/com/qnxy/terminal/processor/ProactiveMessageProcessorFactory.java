package com.qnxy.terminal.processor;

import com.qnxy.terminal.ProactiveAsyncMessageProcessor;
import com.qnxy.terminal.ProactiveSyncMessageProcessor;
import com.qnxy.terminal.message.client.*;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 终端主动消息处理器工厂
 * 根据执行消息类型获取对应的处理器
 *
 * @author Qnxy
 */
@SuppressWarnings("unchecked")
public final class ProactiveMessageProcessorFactory {

    private static final Map<Class<ProactiveSyncMessage>, ProactiveSyncMessageProcessor<ProactiveSyncMessage>> PROACTIVE_SYNC_MESSAGE_PROCESSORS = new ConcurrentHashMap<>();
    private static final Map<Class<ProactiveAsyncMessage>, ProactiveAsyncMessageProcessor<ProactiveAsyncMessage>> PROACTIVE_ASYNC_MESSAGE_PROCESSOR_MAP = new ConcurrentHashMap<>();

    static {
        addAsyncProcessor(Heartbeat.class, new HeartbeatProactiveAsyncMessageProcessor());

        addSyncProcessor(AuthorizationApplication.class, new AuthorizationApplicationProactiveSyncMessageProcessor());
        addSyncProcessor(SwipeCard.class, new SwipeCardProactiveSyncMessageProcessor());

    }

    private ProactiveMessageProcessorFactory() {
    }

    private static <M extends ProactiveAsyncMessage> void addAsyncProcessor(Class<M> messageClass, ProactiveAsyncMessageProcessor<M> messageProcessor) {
        PROACTIVE_ASYNC_MESSAGE_PROCESSOR_MAP.put(
                (Class<ProactiveAsyncMessage>) messageClass,
                (ProactiveAsyncMessageProcessor<ProactiveAsyncMessage>) messageProcessor
        );
    }

    private static <M extends ProactiveSyncMessage> void addSyncProcessor(Class<M> messageClass, ProactiveSyncMessageProcessor<M> messageProcessor) {
        PROACTIVE_SYNC_MESSAGE_PROCESSORS.put(
                (Class<ProactiveSyncMessage>) messageClass,
                (ProactiveSyncMessageProcessor<ProactiveSyncMessage>) messageProcessor
        );
    }

    public static Optional<ProactiveSyncMessageProcessor<ProactiveSyncMessage>> findSyncProcessor(Class<? extends ProactiveSyncMessage> messageClass) {
        return Optional.ofNullable(PROACTIVE_SYNC_MESSAGE_PROCESSORS.get(messageClass));
    }

    public static Optional<ProactiveAsyncMessageProcessor<ProactiveAsyncMessage>> findAsyncProcessor(Class<? extends ProactiveAsyncMessage> messageClass) {
        return Optional.ofNullable(PROACTIVE_ASYNC_MESSAGE_PROCESSOR_MAP.get(messageClass));
    }

}
