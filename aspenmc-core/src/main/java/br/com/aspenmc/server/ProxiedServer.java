package br.com.aspenmc.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Set;
import java.util.UUID;

@Getter
public class ProxiedServer {

    private final String serverAddress;
    private final int serverPort;

    private final String serverId;
    private final String serverType;

    @Setter
    private Set<UUID> players;

    @Setter
    private int maxPlayers;
    @Setter
    private int playersRecord;

    @Setter
    private boolean joinEnabled;

    @Setter
    protected GameState state;
    @Setter
    protected int time;
    @Setter
    protected String mapName;

    @Setter
    private long startTime;

    public ProxiedServer(String serverAddress, int serverPort, String serverId, ServerType serverType, Set<UUID> players, int maxPlayers, boolean joinEnabled, GameState state, int time, String mapName) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.serverId = serverId.toLowerCase();
        this.serverType = serverType.getName();
        this.players = players;
        this.maxPlayers = maxPlayers;
        this.joinEnabled = joinEnabled;
        this.state = state;
        this.time = time;
        this.mapName = mapName;
        this.startTime = System.currentTimeMillis();
    }

    public ProxiedServer(String serverAddress, int serverPort, String serverId, ServerType serverType, Set<UUID> players, int maxPlayers, boolean joinEnabled) {
        this(serverAddress, serverPort, serverId, serverType, players, maxPlayers, joinEnabled, GameState.UNKNOWN, 0,
             "Unknown");
    }

    public void setOnlinePlayers(Set<UUID> onlinePlayers) {
        this.players = onlinePlayers;
    }

    public void joinPlayer(UUID uuid) {
        players.add(uuid);
        playersRecord = Math.max(playersRecord, players.size());
    }

    public void leavePlayer(UUID uuid) {
        players.remove(uuid);
    }

    public int getOnlinePlayers() {
        return players.size();
    }

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    public ServerInfo getServerInfo() {
        return ProxyServer.getInstance().getServerInfo(serverId);
    }

    public boolean canBeSelected() {
        if (state == GameState.UNKNOWN || state.isStarting()) {
            return !isFull();
        }

        return false;
    }

    public int getActualNumber() {
        return getOnlinePlayers();
    }

    public ServerType getServerType() {
        return ServerType.getByName(serverType);
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public enum GameState {

        UNKNOWN,

        WAITING(true),
        PREGAME(true),
        STARTING(true),

        INVENCIBILITY(true),
        GAMETIME,

        DEATHMATCH(true),
        WINNING;

        private boolean decrement;

        public boolean isStarting() {
            return ordinal() >= WAITING.ordinal() && ordinal() <= STARTING.ordinal();
        }
    }
}
