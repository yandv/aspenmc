package br.com.aspenmc.bungee.listener;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bungee.BungeeMain;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.member.configuration.LoginConfiguration;
import br.com.aspenmc.server.ProxiedServer;
import br.com.aspenmc.server.ServerType;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.SearchServerEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class ServerListener implements Listener {

    @EventHandler
    public void onSearchServer(SearchServerEvent event) {
        ProxiedPlayer player = event.getPlayer();
        LoginConfiguration.LoginResult loginResult = CommonPlugin.getInstance().getMemberManager()
                                                                 .getMemberById(player.getUniqueId())
                                                                 .map(Member::getLoginConfiguration)
                                                                 .map(LoginConfiguration::reloadSession)
                                                                 .orElse(LoginConfiguration.LoginResult.NOT_LOGGED);
        ServerType serverType;

        switch (loginResult) {
        case SESSION_EXPIRED:
        case NOT_LOGGED:
            serverType = ServerType.LOGIN;
            break;
        case SESSION_RESTORED:
            player.sendMessage("§aSua sessão foi restaurada.");
        default:
            serverType = ServerType.LOBBY;
            break;
        }

        ProxiedServer server = CommonPlugin.getInstance().getServerManager().getBalancer(serverType).next();

        if (server == null || server.getServerInfo() == null) {
            event.setCancelled(true);
            event.setCancelMessage(
                    "§c§lASPENMC\n§c\n§cNenhum servidor disponível no momento.\n§cTente novamente em alguns instantes" +
                            ".");
            return;
        }

        CommonPlugin.getInstance()
                    .debug("Sending the player " + player.getName() + " to the server " + server.getServerId() + " (" +
                            server.getServerInfo().getName() + ")");
        event.setServer(server.getServerInfo());
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(player.getUniqueId()).orElse(null);

        if (member == null) return;

        boolean logged = member.getLoginConfiguration().isLogged();

        if (logged) return;

        ProxiedServer toServer = CommonPlugin.getInstance().getServerManager().getServer(event.getTarget().getName());

        if (toServer == null || toServer.getServerType() == ServerType.LOGIN) return;

        event.setCancelled(true);
        player.sendMessage("§cSua sessão ainda não permite o acesso a este servidor.");
    }

    @EventHandler
    public void onProxyServer(ProxyPingEvent event) {
        ServerPing serverPing = event.getResponse();
        String serverIp = getServerIp(event.getConnection());
        ProxiedServer server = CommonPlugin.getInstance().getServerManager().getServer(serverIp);

        if (BungeeMain.getInstance().isMaintenance()) {
            serverPing.setDescription(BungeeMain.getInstance().getMotdManager().getMaintenance().getAsString());
            return;
        }

        serverPing.getPlayers().setOnline(CommonPlugin.getInstance().getServerManager().getTotalCount());
        serverPing.getPlayers().setMax(1500);

        if (server == null || server.getServerType() == ServerType.BUNGEECORD) {
            serverPing.getPlayers().setSample(new ServerPing.PlayerInfo[] {
                    new ServerPing.PlayerInfo("§e" + CommonConst.WEBSITE, UUID.randomUUID()) });
            serverPing.setDescription(BungeeMain.getInstance().getMotdManager().getRandomMotd().getAsString());
        } else {
            event.registerIntent(BungeeMain.getInstance());
            server.getServerInfo().ping((realPing, throwable) -> {
                if (throwable == null) {
                    serverPing.getPlayers().setMax(realPing.getPlayers().getMax());
                    serverPing.getPlayers().setOnline(realPing.getPlayers().getOnline());
                    serverPing.setDescription(realPing.getDescription());
                } else {
                    serverPing.getPlayers().setSample(new ServerPing.PlayerInfo[] {
                            new ServerPing.PlayerInfo("§e" + CommonConst.WEBSITE, UUID.randomUUID()) });
                    serverPing.setDescription(BungeeMain.getInstance().getMotdManager().getRandomMotd().getAsString());
                }

                event.completeIntent(BungeeMain.getInstance());
            });
        }
    }

    private String getServerIp(PendingConnection con) {
        if (con == null || con.getVirtualHost() == null) {
            return "";
        }

        return con.getVirtualHost().getHostName().toLowerCase();
    }
}
