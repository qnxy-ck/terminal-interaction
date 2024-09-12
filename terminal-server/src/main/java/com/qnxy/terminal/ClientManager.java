package com.qnxy.terminal;

import com.qnxy.terminal.client.TerminalClient;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 终端连接信息管理
 *
 * @author Qnxy
 */
public final class ClientManager {

    private static final Map<Long, TerminalClient> clients = new ConcurrentHashMap<>();

    /**
     * 添加新的终端连接
     *
     * @param id             唯一id
     * @param terminalClient 终端连接
     */
    public static void addClient(Long id, TerminalClient terminalClient) {
        clients.put(id, terminalClient);
    }

    /**
     * 根据id获取一个连接
     */
    public static Optional<TerminalClient> findClient(Long id) {
        return Optional.ofNullable(clients.get(id));
    }

    /**
     * 删除一个终端信息, 根据id
     */
    public static void removeClient(Long id) {
        Optional.ofNullable(id).ifPresent(client -> clients.remove(id));
    }

    /**
     * 当前客户端数量
     */
    public static int currentClientCount() {
        return clients.size();
    }

}
