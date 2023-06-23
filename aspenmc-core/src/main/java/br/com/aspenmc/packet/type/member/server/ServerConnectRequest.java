package br.com.aspenmc.packet.type.member.server;

import br.com.aspenmc.server.ProxiedServer;
import lombok.Getter;
import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.packet.Packet;
import br.com.aspenmc.server.ServerType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;

import java.util.Arrays;
import java.util.UUID;

@Getter
public class ServerConnectRequest extends Packet {

    private final UUID playerId;
    private final String[] servers;

    public ServerConnectRequest(UUID playerId, String... servers) {
        bungeecord();
        this.playerId = playerId;
        this.servers = servers;
    }

    public ServerConnectRequest(UUID playerId, ServerType... servers) {
        bungeecord();
        this.playerId = playerId;
        this.servers = Arrays.stream(servers).map(ServerType::name).toArray(String[]::new);
    }

    @Override
    public void receive() {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerId);

        if (player == null) return;

        if (servers.length == 1) {
            ProxiedServer server = getServer(servers[0]);

            if (server == null) {
                callback(null, ServerConnectResponse.ResponseType.SERVER_NOT_FOUND);
                return;
            }

            if (server.getServerInfo() == null) {
                callback(null, ServerConnectResponse.ResponseType.SERVER_UNAVAILABLE);
                return;
            }

            if (server.isFull() && !player.hasPermission(CommonConst.SERVER_FULL_PERMISSION)) {
                callback(null, ServerConnectResponse.ResponseType.SERVER_FULL);
                return;
            }

            if (!server.canBeSelected() && !player.hasPermission(CommonConst.ADMIN_MODE_PERMISSION)) {
                callback(null, ServerConnectResponse.ResponseType.INSSUFICIENT_PERMISSIONS);
                return;
            }

            player.connect(server.getServerInfo());
            callback(server.getServerId(), ServerConnectResponse.ResponseType.SUCCESS);
            return;
        }

        for (String server : servers) {
            ProxiedServer pServer = getServer(server);
            if (connect(player, pServer)) {
                callback(pServer.getServerId(), ServerConnectResponse.ResponseType.SUCCESS);
                return;
            }
        }

        callback(null, ServerConnectResponse.ResponseType.SERVER_NOT_FOUND);
    }

    private void callback(String serverId, ServerConnectResponse.ResponseType responseType) {
        CommonPlugin.getInstance().getServerData().sendPacket(
                new ServerConnectResponse(playerId, serverId, responseType).id(getId()).server(getSource()));
    }

    public ProxiedServer getServer(String server) {
        ServerType serverType = ServerType.getByName(server);

        if (serverType == null) {
            return CommonPlugin.getInstance().getServerManager().getServer(server);
        } else {
            return CommonPlugin.getInstance().getServerManager().getBalancer(serverType).next();
        }
    }

    public boolean connect(ProxiedPlayer player, ProxiedServer server) {
        if (server == null || server.getServerInfo() == null) {
            return false;
        }

        if (server.isFull() && !player.hasPermission(CommonConst.SERVER_FULL_PERMISSION)) {
            return false;
        }

        if (!server.canBeSelected() && !player.hasPermission(CommonConst.ADMIN_MODE_PERMISSION)) {
            return false;
        }

        player.connect(server.getServerInfo(), ServerConnectEvent.Reason.COMMAND);
        return true;
    }
}