package com.qnxy.terminal;

import com.qnxy.terminal.client.Client;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Qnxy
 */
public final class ClientManager {

    private static final Map<Long, Client> clients = new ConcurrentHashMap<>();


    public static void addClient(Long id, Client client) {
        clients.put(id, client);
    }

    public static Optional<Client> findClient(Long id) {
        return Optional.ofNullable(clients.get(id));
    }

    public static void removeClient(Long id) {
        Optional.ofNullable(id).ifPresent(client -> clients.remove(id));
    }


}
