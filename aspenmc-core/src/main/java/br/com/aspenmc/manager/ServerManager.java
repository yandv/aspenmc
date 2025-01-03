package br.com.aspenmc.manager;

import br.com.aspenmc.server.ProxiedServer;
import br.com.aspenmc.server.ServerType;
import br.com.aspenmc.server.loadbalancer.BaseBalancer;
import br.com.aspenmc.server.loadbalancer.impl.LeastConnection;
import br.com.aspenmc.server.loadbalancer.impl.MostConnection;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * ServerManager to control and loadbalance all connected servers
 *
 * @author yandv
 * @since 1.0
 */

@Getter
public class ServerManager {

    private final Map<String, ProxiedServer> activeServers;
    private final Map<ServerType, BaseBalancer<ProxiedServer>> balancers;

    public ServerManager() {
        balancers = new HashMap<>();
        activeServers = new HashMap<>();

        for (ServerType serverType : ServerType.values())
            if (serverType != ServerType.DISCORD) {
                if (serverType.isLobby()) {
                    balancers.put(serverType, new LeastConnection<>());
                } else {
                    balancers.put(serverType, new MostConnection<>());
                }
            }
    }

    public void addActiveServer(ProxiedServer server) {
        activeServers.put(server.getServerId().toLowerCase(), server);
        addToBalancers(server);
    }

    public void addToBalancers(ProxiedServer server) {
        BaseBalancer<ProxiedServer> balancer = getBalancer(server.getServerType());

        if (balancer == null) return;

        balancer.add(server);
    }

    public void removeActiveServer(String str) {
        if (getServer(str) != null) {
            removeFromBalancers(getServer(str));
        }

        activeServers.remove(str.toLowerCase());
    }

    public void removeFromBalancers(ProxiedServer serverId) {
        BaseBalancer<ProxiedServer> balancer = getBalancer(serverId.getServerType());
        if (balancer != null) {
            balancer.remove(serverId.getServerId().toLowerCase());
        }
    }

    public ProxiedServer getServer(String serverName) {
        return activeServers.get(serverName.toLowerCase());
    }

    public boolean hasServer(String serverId) {
        return activeServers.containsKey(serverId.toLowerCase());
    }

    public ProxiedServer getServerByName(String serverName) {
        return activeServers.values().stream().filter(proxiedServer -> proxiedServer.getServerId().toLowerCase()
                                                                                    .startsWith(
                                                                                            serverName.toLowerCase()))
                            .findFirst().orElse(null);
    }

    public Collection<ProxiedServer> getServers() {
        return activeServers.values();
    }

    public BaseBalancer<ProxiedServer> getBalancer(ServerType type) {
        return balancers.getOrDefault(type, null);
    }

    public int getTotalCount() {
        return activeServers.values().stream().mapToInt(ProxiedServer::getOnlinePlayers).sum();
    }

    public int getTotalNumber(ServerType... serverTypes) {
        return Arrays.stream(serverTypes).mapToInt(serverType -> getBalancer(serverType).getTotalNumber()).sum();
    }

    public ProxiedServer getActiveServer(ServerType... servers) {
        for (ServerType serverType : servers) {
            ProxiedServer server = getBalancer(serverType).next();
            if (server != null) return server;
        }

        return null;
    }
}