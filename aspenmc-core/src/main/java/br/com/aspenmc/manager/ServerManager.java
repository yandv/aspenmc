package br.com.aspenmc.manager;

import br.com.aspenmc.server.ProxiedServer;
import br.com.aspenmc.server.ServerType;
import br.com.aspenmc.server.loadbalancer.BaseBalancer;
import br.com.aspenmc.server.loadbalancer.impl.LeastConnection;
import br.com.aspenmc.server.loadbalancer.impl.MostConnection;
import lombok.Getter;

import java.util.*;

/**
 * ServerManager to control and loadbalance all connected servers
 *
 * @author yandv
 * @since 1.0
 */

@Getter
public class ServerManager {

    private Map<String, ProxiedServer> activeServers;
    private Map<ServerType, BaseBalancer<ProxiedServer>> balancers;

    public ServerManager() {
        balancers = new HashMap<>();
        activeServers = new HashMap<>();

        for (ServerType serverType : ServerType.values())
            if (serverType != ServerType.DISCORD) {
                balancers.put(serverType,
                              serverType.name().contains("LOBBY") ? new LeastConnection<>() : new MostConnection<>());
            }
    }

    public BaseBalancer<ProxiedServer> getBalancer(ServerType type) {
        if (type == null) return null;

        return balancers.get(type);
    }

    public void putBalancer(ServerType type, BaseBalancer<ProxiedServer> balancer) {
        balancers.put(type, balancer);
    }

    public ProxiedServer addActiveServer(String serverAddress, int serverPort, String serverIp, ServerType type, int maxPlayers, boolean joinEnabled, ProxiedServer.GameState gameState, int time, String mapName, long startTime) {
        return updateActiveServer(serverAddress, serverPort, serverIp, type, new HashSet<>(), maxPlayers, joinEnabled,
                                  gameState, time, mapName, startTime);
    }

    public void addActiveServer(ProxiedServer server) {
        activeServers.put(server.getServerId().toLowerCase(), server);
        addToBalancers(server.getServerId(), server);
    }

    public ProxiedServer updateActiveServer(String serverAddress, int serverPort, String serverId, ServerType type, Set<UUID> onlinePlayers, int maxPlayers, boolean joinEnabled, ProxiedServer.GameState gameState, int time, String mapName, long startTime) {
        ProxiedServer server = activeServers.get(serverId);

        if (server == null) {
            server = createInstance(serverAddress, serverPort, serverId, type, onlinePlayers, maxPlayers, joinEnabled,
                                    gameState, time, mapName);
            server.setStartTime(startTime);
            activeServers.put(serverId.toLowerCase(), server);
        }

        server.setOnlinePlayers(onlinePlayers);
        server.setJoinEnabled(joinEnabled);

        addToBalancers(serverId, server);
        return server;
    }

    private ProxiedServer createInstance(String serverAddress, int serverPort, String serverId, ServerType type, Set<UUID> onlinePlayers, int maxPlayers, boolean joinEnabled, ProxiedServer.GameState gameState, int time, String mapName) {
        return new ProxiedServer(serverAddress, serverPort, serverId, type, onlinePlayers, maxPlayers, joinEnabled);
    }

    public ProxiedServer getServer(String serverName) {
        return activeServers.get(serverName.toLowerCase());
    }

    public ProxiedServer getServerByName(String serverName) {
        for (ProxiedServer proxiedServer : activeServers.values())
            if (proxiedServer.getServerId().toLowerCase().startsWith(serverName.toLowerCase())) {
                return proxiedServer;
            }

        return activeServers.get(serverName.toLowerCase());
    }

    public Collection<ProxiedServer> getServers() {
        return activeServers.values();
    }

    public void removeActiveServer(String str) {
        if (getServer(str) != null) {
            removeFromBalancers(getServer(str));
        }

        activeServers.remove(str.toLowerCase());
    }

    public void addToBalancers(String serverId, ProxiedServer server) {
        BaseBalancer<ProxiedServer> balancer = getBalancer(server.getServerType());

        if (balancer == null) {
            return;
        }

        balancer.add(serverId.toLowerCase(), server);
    }

    public void removeFromBalancers(ProxiedServer serverId) {
        BaseBalancer<ProxiedServer> balancer = getBalancer(serverId.getServerType());
        if (balancer != null) {
            balancer.remove(serverId.getServerId().toLowerCase());
        }
    }

    public int getTotalNumber(ServerType... serverTypes) {
        int number = 0;

        for (ServerType serverType : serverTypes)
            number += getBalancer(serverType).getTotalNumber();

        return number;
    }

    public int getTotalNumber(List<ServerType> types) {
        int players = 0;

        for (ServerType type : types) {
            players += getBalancer(type).getTotalNumber();
        }

        return players;
    }

    public Set<UUID> getOnlinePlayers() {
        Set<UUID> players = new HashSet<>();

        for (ProxiedServer server : activeServers.values()) {
            players.addAll(server.getPlayers());
        }

        return players;
    }

    public Set<UUID> getProxyPlayers() {
        return activeServers.values().stream().filter(server -> server.getServerType() == ServerType.BUNGEECORD)
                            .map(ProxiedServer::getPlayers).reduce(new HashSet<>(), (acc, id) -> {
                    acc.addAll(id);
                    return acc;
                });
    }

    public int getTotalCount() {
        return activeServers.values().stream().mapToInt(ProxiedServer::getOnlinePlayers).sum();
    }

    public int getCurrentPlayersCount() {
        return getTotalCount();
    }
}