package br.com.aspenmc.bungee.manager;

import br.com.aspenmc.manager.ServerManager;
import br.com.aspenmc.server.ServerType;
import br.com.aspenmc.server.loadbalancer.server.ProxiedServer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;

public class BungeeServerManager extends ServerManager {

    @Override
    public ProxiedServer addActiveServer(String serverAddress, int serverPort, String serverIp, ServerType type, int maxPlayers, boolean joinEnabled, ProxiedServer.GameState gameState, int time, String mapName, long startTime) {
        ProxiedServer server = super.addActiveServer(serverAddress, serverPort, serverIp, type, maxPlayers, joinEnabled,
                                                     gameState, time, mapName, startTime);

        if (server.getServerType() != ServerType.BUNGEECORD &&
            !ProxyServer.getInstance().getServers().containsKey(serverIp.toLowerCase())) {
            ServerInfo localServerInfo = ProxyServer.getInstance().constructServerInfo(serverIp.toLowerCase(),
                                                                                       new InetSocketAddress(
                                                                                               serverAddress,
                                                                                               serverPort),
                                                                                       "Restarting", false);

            ProxyServer.getInstance().getServers().put(serverIp.toLowerCase(), localServerInfo);
        }

        return server;
    }

    @Override
    public void addActiveServer(ProxiedServer server) {
        super.addActiveServer(server);

        if (server.getServerType() != ServerType.BUNGEECORD &&
            !ProxyServer.getInstance().getServers().containsKey(server.getServerId().toLowerCase())) {
            ServerInfo localServerInfo = ProxyServer.getInstance()
                                                    .constructServerInfo(server.getServerId().toLowerCase(),
                                                                         new InetSocketAddress(
                                                                                 server.getServerAddress(),
                                                                                 server.getServerPort()), "Restarting",
                                                                         false);

            ProxyServer.getInstance().getServers().put(server.getServerId().toLowerCase(), localServerInfo);
        }
    }

    @Override
    public void removeActiveServer(String str) {
        super.removeActiveServer(str);

        ProxyServer.getInstance().getServers().remove(str.toLowerCase());
    }
}
