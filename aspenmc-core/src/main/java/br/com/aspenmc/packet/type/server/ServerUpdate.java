package br.com.aspenmc.packet.type.server;

import br.com.aspenmc.server.ProxiedServer;
import br.com.aspenmc.server.ServerType;
import lombok.Getter;
import br.com.aspenmc.CommonPlugin;

import java.util.UUID;

@Getter
public class ServerUpdate extends Server {

    private final UpdateType updateType;

    private UUID playerId;
    private int maxPlayers;

    private ProxiedServer.GameState state;
    private int time;
    private String mapName;

    public ServerUpdate(String serverId, ServerType serverType, UUID playerId, UpdateType updateType) {
        super(serverId, serverType);
        this.playerId = playerId;
        this.updateType = updateType;
    }

    public ServerUpdate(String serverId, ServerType serverType, int maxPlayers) {
        super(serverId, serverType);
        this.maxPlayers = maxPlayers;
        this.updateType = UpdateType.MAX_PLAYERS;
    }

    public ServerUpdate(String serverId, ServerType serverType, ProxiedServer.GameState state, int time, String mapName) {
        super(serverId, serverType);
        this.state = state;
        this.time = time;
        this.mapName = mapName;
        this.updateType = UpdateType.STATE;
    }

    @Override
    public void receive() {
        if (!CommonPlugin.getInstance().isServerLog()) return;

        ProxiedServer server = CommonPlugin.getInstance().getServerManager().getServer(getServerId());

        if (server == null) return;

        switch (updateType) {
        case JOIN:
            server.joinPlayer(playerId);
            break;
        case LEAVE:
            server.leavePlayer(playerId);
            break;
        case MAX_PLAYERS:
            server.setMaxPlayers(maxPlayers);
            break;
        case STATE:
            server.setState(state);
            server.setTime(time);
            server.setMapName(mapName);
            break;
        }
    }

    public enum UpdateType {
        JOIN,
        LEAVE,
        MAX_PLAYERS,

        STATE
    }
}
