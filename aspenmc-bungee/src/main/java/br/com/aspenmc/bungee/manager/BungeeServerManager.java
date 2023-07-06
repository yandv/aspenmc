package br.com.aspenmc.bungee.manager;

import br.com.aspenmc.manager.ServerManager;
import br.com.aspenmc.server.ProxiedServer;
import br.com.aspenmc.server.ServerType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;

public class BungeeServerManager extends ServerManager {

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
