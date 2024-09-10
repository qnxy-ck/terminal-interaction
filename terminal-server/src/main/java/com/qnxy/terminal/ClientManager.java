package com.qnxy.terminal;

import com.qnxy.terminal.client.TerminalClient;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Qnxy
 */
public final class ClientManager {

    private static final Map<Long, TerminalClient> clients = new ConcurrentHashMap<>();


    public static void addClient(Long id, TerminalClient terminalClient) {
        clients.put(id, terminalClient);
    }

    public static Optional<TerminalClient> findClient(Long id) {
        return Optional.ofNullable(clients.get(id));
    }

    public static void removeClient(Long id) {
        Optional.ofNullable(id).ifPresent(client -> clients.remove(id));
    }

    public static int countClients() {
        return clients.size();
    }


}
