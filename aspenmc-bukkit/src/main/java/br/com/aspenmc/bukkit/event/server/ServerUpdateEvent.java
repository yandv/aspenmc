package br.com.aspenmc.bukkit.event.server;

import br.com.aspenmc.bukkit.event.NormalEvent;
import br.com.aspenmc.packet.type.server.ServerUpdate;
import br.com.aspenmc.server.ProxiedServer;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ServerUpdateEvent extends NormalEvent {

    private String serverId;

    private ServerUpdate.UpdateType updateType;

    private UUID playerId;
    private int maxPlayers;

    private ProxiedServer.GameState gameState;
    private int time;
    private String mapName;

    public ServerUpdateEvent(ServerUpdate serverUpdate) {
        this.serverId = serverUpdate.getServerId();
        this.updateType = serverUpdate.getUpdateType();
        this.playerId = serverUpdate.getPlayerId();
        this.maxPlayers = serverUpdate.getMaxPlayers();
        this.gameState = serverUpdate.getState();
        this.time = serverUpdate.getTime();
        this.mapName = serverUpdate.getMapName();
    }
}
