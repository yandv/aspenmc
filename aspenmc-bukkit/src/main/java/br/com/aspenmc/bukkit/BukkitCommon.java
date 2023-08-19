package br.com.aspenmc.bukkit;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlatform;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.backend.type.RedisConnection;
import br.com.aspenmc.bukkit.command.BukkitCommandFramework;
import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.player.PlayerChangeLeagueEvent;
import br.com.aspenmc.bukkit.event.player.group.PlayerChangedGroupEvent;
import br.com.aspenmc.bukkit.event.server.GameStateChangeEvent;
import br.com.aspenmc.bukkit.event.server.ServerUpdateEvent;
import br.com.aspenmc.bukkit.listener.*;
import br.com.aspenmc.bukkit.manager.*;
import br.com.aspenmc.bukkit.networking.BukkitPubSub;
import br.com.aspenmc.bukkit.permission.regex.RegexPermissions;
import br.com.aspenmc.bukkit.entity.BukkitConsoleSender;
import br.com.aspenmc.bukkit.protocol.impl.LimiterInjector;
import br.com.aspenmc.bukkit.protocol.impl.MessageInjector;
import br.com.aspenmc.bukkit.utils.hologram.impl.RankingHologram;
import br.com.aspenmc.bukkit.utils.scheduler.UpdateScheduler;
import br.com.aspenmc.entity.member.gamer.Gamer;
import br.com.aspenmc.entity.member.status.StatusType;
import br.com.aspenmc.packet.type.member.MemberGroupChange;
import br.com.aspenmc.packet.type.member.server.ServerConnectRequest;
import br.com.aspenmc.packet.type.member.server.ServerConnectResponse;
import br.com.aspenmc.packet.type.server.ServerUpdate;
import br.com.aspenmc.packet.type.server.command.BungeeCommandRequest;
import br.com.aspenmc.packet.type.server.command.BungeeCommandResponse;
import br.com.aspenmc.server.ProxiedServer;
import br.com.aspenmc.server.ServerType;
import br.com.aspenmc.utils.ProtocolVersion;
import com.comphenix.protocol.ProtocolLibrary;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

@Getter
public abstract class BukkitCommon extends JavaPlugin implements CommonPlatform {

    @Getter
    private static BukkitCommon instance;

    protected CommonPlugin plugin;

    protected RegexPermissions regexPerms;

    protected CharacterManager characterManager;
    protected CombatlogManager combatlogManager;
    protected CooldownManager cooldownManager;
    protected HologramManager hologramManager;
    protected LocationManager locationManager;
    protected VanishManager vanishManager;

    private ProxiedServer.GameState state = ProxiedServer.GameState.UNKNOWN;
    private int time;
    @Setter
    private boolean consoleControl = true;
    @Setter
    private boolean timerEnabled;
    private String mapName;

    @Setter
    private boolean tagControl = true;

    @Setter
    private Set<Map.Entry<String, Class<? extends Gamer<Player>>>> gamerList;
    private Set<StatusType> preloadedStatus;

    @Setter
    private boolean saveGamers = true;

    @Setter
    private boolean removePlayerDat = true;

    private boolean pluginEnabled = true;

    @Override
    public void onLoad() {
        try {
            instance = this;

            saveDefaultConfig();

            plugin = new CommonPlugin(this, getLogger());

            plugin.setConsoleSender(new BukkitConsoleSender());

            plugin.setServerAddress(Bukkit.getIp());
            plugin.setServerPort(Bukkit.getPort());

            plugin.setServerId(getConfig().getString("serverId", "unnamed-server"));
            plugin.setServerType(Optional.ofNullable(ServerType.getByName(getConfig().getString("serverType")))
                                         .orElse(ServerType.LOBBY));

            plugin.startConnection();
            plugin.getServerService().startServer(Bukkit.getMaxPlayers());

            new LimiterInjector();
            new MessageInjector();

            plugin.debug("Starting the server " + plugin.getServerId() + " (" + plugin.getServerType().name() + ").");

            super.onLoad();
            onCompleteLoad();
        } catch (Exception e) {
            getLogger().severe("An error occurred while loading the plugin.");
            getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
            pluginEnabled = false;
        }
    }

    public void onCompleteLoad() {

    }

    @Override
    public void onEnable() {
        if (!pluginEnabled) {
            getLogger().severe("The plugin was not enabled due to an error in the loading process.");
            Bukkit.shutdown();
            return;
        }

        try {
            regexPerms = new RegexPermissions();

            runAsync(new RedisConnection.PubSubListener(plugin.getRedisConnection(), new BukkitPubSub(),
                    CommonConst.SERVER_PACKET_CHANNEL));

            characterManager = new CharacterManager();
            combatlogManager = new CombatlogManager();
            cooldownManager = new CooldownManager();
            hologramManager = new HologramManager();
            locationManager = new LocationManager();
            vanishManager = new VanishManager();

            if (plugin.getPermissionManager().getTags().isEmpty()) {
                setTagControl(false);
            }

            gamerList = new HashSet<>();
            preloadedStatus = new HashSet<>();

            registerPacketHandlers();
            registerListeners();

            runLater(this::registerCommands, 7L);
            runTimer(new UpdateScheduler(), 1, 1);

            if (plugin.isServerLog()) {
                plugin.loadServers();
            }

            plugin.debug("Started the server " + plugin.getServerId() + " (" + plugin.getServerType().name() + ").");

            super.onEnable();
            onCompleteStart();
        } catch (Exception e) {
            getLogger().severe("An error occurred while enabling the plugin.");
            getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
            Bukkit.shutdown();
        }
    }

    public void onCompleteStart() {

    }

    @Override
    public void onDisable() {
        try {
            plugin.getServerService().stopServer();
            plugin.debug("Stopped the server " + plugin.getServerId() + " (" + plugin.getServerType().name() + ").");

            super.onDisable();
        } catch (Exception e) {
            getLogger().severe("An error occurred while disabling the plugin.");
            getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
        }
    }

    public void updateState(ProxiedServer.GameState state, int time) {
        ProxiedServer.GameState oldState = this.state;
        this.state = state;
        this.time = time;
        CommonPlugin.getInstance().getPacketManager().sendPacketAsync(
                new ServerUpdate(CommonPlugin.getInstance().getServerId(), CommonPlugin.getInstance().getServerType(),
                        state, time, mapName));

        if (oldState != state) {
            Bukkit.getPluginManager().callEvent(new GameStateChangeEvent(oldState, state));
            CommonPlugin.getInstance()
                        .debug("The game state changed from " + oldState.name() + " to " + state.name() + ".");
        }
    }

    public void updateState(ProxiedServer.GameState state, int time, String mapName) {
        ProxiedServer.GameState oldState = this.state;

        this.state = state;
        this.time = time;
        this.mapName = mapName;

        CommonPlugin.getInstance().getPacketManager().sendPacketAsync(
                new ServerUpdate(CommonPlugin.getInstance().getServerId(), CommonPlugin.getInstance().getServerType(),
                        state, time, mapName));

        if (oldState != state) {
            Bukkit.getPluginManager().callEvent(new GameStateChangeEvent(oldState, state));
            CommonPlugin.getInstance()
                        .debug("The game state changed from " + oldState.name() + " to " + state.name() + ".");
        }
    }

    public void updateState(ProxiedServer.GameState state) {
        ProxiedServer.GameState oldState = this.state;

        if (oldState != state) {
            this.state = state;
            CommonPlugin.getInstance().getPacketManager().sendPacketAsync(
                    new ServerUpdate(CommonPlugin.getInstance().getServerId(),
                            CommonPlugin.getInstance().getServerType(), state, time, mapName));
            Bukkit.getPluginManager().callEvent(new GameStateChangeEvent(oldState, state));
            CommonPlugin.getInstance()
                        .debug("The game state changed from " + oldState.name() + " to " + state.name() + ".");
        }
    }

    public void updateTime(int time) {
        this.time = time;
        CommonPlugin.getInstance().getPacketManager().sendPacketAsync(
                new ServerUpdate(CommonPlugin.getInstance().getServerId(), CommonPlugin.getInstance().getServerType(),
                        state, time, mapName));
    }

    public void updateMapName(String mapName) {
        this.mapName = mapName;
        CommonPlugin.getInstance().getPacketManager().sendPacketAsync(
                new ServerUpdate(CommonPlugin.getInstance().getServerId(), CommonPlugin.getInstance().getServerType(),
                        state, time, mapName));
    }

    public void register(RankingHologram.Loader loader, String... texts) {
        getHologramManager().loadHologram(new RankingHologram(loader, texts));
    }

    public void loadGamer(String gamerId, Class<? extends Gamer<Player>> gamerClass) {
        gamerList.add(new AbstractMap.SimpleEntry<>(gamerId, gamerClass));
    }

    public void loadStatus(StatusType statusType) {
        preloadedStatus.add(statusType);
    }

    public void sendPlayerToServer(Player player, String... serversIds) {
        plugin.getPacketManager().waitPacket(ServerConnectResponse.class,
                plugin.getServerService().sendPacket(new ServerConnectRequest(player.getUniqueId(), serversIds)),
                packet -> handleServerConnectResponse(player, packet));
    }

    public void sendPlayerToServer(Player player, ServerType... types) {
        plugin.getPacketManager().waitPacket(ServerConnectResponse.class,
                plugin.getServerService().sendPacket(new ServerConnectRequest(player.getUniqueId(), types)),
                packet -> handleServerConnectResponse(player, packet));
    }

    private void handleServerConnectResponse(Player player, ServerConnectResponse packet) {
        if (packet.getResponseType() == ServerConnectResponse.ResponseType.SUCCESS) return;

        String message;

        switch (packet.getResponseType()) {
        case INSSUFICIENT_PERMISSIONS:
            message = "§cSomente membros da equipe podem entrar no servidor no momento.";
            break;
        case SERVER_NOT_FOUND:
            message = "§cO servidor não existe ou não está disponível.";
            break;
        case UNKNOWN_ERROR:
        case SERVER_UNAVAILABLE:
            message = "§cO servidor não está disponível no momento.";
            break;
        case SERVER_FULL:
            message = "§cO servidor está cheio no momento.";
            break;
        case STATE_NOT_ALLOWS_TO_CONNECT:
            message = "§cSomente jogadores pagantes podem entrar no momento.";
            break;
        default:
            return;
        }

        player.sendMessage(message);
    }

    public void registerPacketHandlers() {
        plugin.getPacketManager().onEnable();

        plugin.getPacketManager().registerHandler(MemberGroupChange.class, packet -> {
            plugin.getMemberManager().getMemberById(packet.getPlayerId(), BukkitMember.class).ifPresent(member -> {
                member.resetHigherGroup();
                Bukkit.getPluginManager().callEvent(
                        new PlayerChangedGroupEvent(member, packet.getGroupName(), packet.getExpiresAt(),
                                packet.getDuration(),
                                PlayerChangedGroupEvent.GroupAction.valueOf(packet.getGroupAction().name())));
            });
        });

        plugin.getPacketManager().registerHandler(BungeeCommandResponse.class, packet -> {
            for (BungeeCommandResponse.NormalCommand command : packet.getCommands()) {
                if (!command.getName().contains("\\.")) {
                    BukkitCommandFramework.INSTANCE.getKnownCommands().put(command.getName(),
                            BukkitCommandFramework.INSTANCE.createCommand(command.getName(), command.getName(),
                                    command.getPermission()));
                }

                for (String alias : command.getAliases()) {
                    if (alias.contains("\\.")) continue;
                    BukkitCommandFramework.INSTANCE.getKnownCommands().put(alias,
                            BukkitCommandFramework.INSTANCE.createCommand(alias, command.getName(),
                                    command.getPermission()));
                }
            }
        });

        plugin.getPacketManager().registerHandler(ServerUpdate.class,
                packet -> Bukkit.getPluginManager().callEvent(new ServerUpdateEvent(packet)));

        plugin.getPluginPlatform()
              .runLater(() -> plugin.getPacketManager().sendPacket(new BungeeCommandRequest()), 12L);
    }

    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new ActionItemListener(), this);
        Bukkit.getPluginManager().registerEvents(new CharacterListener(), this);
        Bukkit.getPluginManager().registerEvents(new HologramListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new MemberListener(), this);
        Bukkit.getPluginManager().registerEvents(new MenuListener(), this);
        Bukkit.getPluginManager().registerEvents(new MoveListener(), this);
        Bukkit.getPluginManager().registerEvents(new PermissionListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new SoupListener(), this);
        Bukkit.getPluginManager().registerEvents(new TagListener(), this);
        Bukkit.getPluginManager().registerEvents(new VanishListener(), this);

        plugin.getStatusManager().setLeagueChangeObserver((status, oldLeague, newLeague) -> {
            BukkitMember member = plugin.getMemberManager().getMemberById(status.getUniqueId(), BukkitMember.class)
                                        .orElse(null);

            if (member == null) return;

            Bukkit.getPluginManager()
                  .callEvent(new PlayerChangeLeagueEvent(member.getPlayer(), member, status, oldLeague, newLeague));
            member.setTag(member.getTag().orElse(null));
        });
    }

    public void registerCommands() {
        BukkitCommandFramework.INSTANCE.unregisterCommands("me", "tellraw", "?", "whitelist", "gamemode", "clear",
                "tps", "ban", "ban-ip", "banlist", "pardon", "pardon-ip", "stop", "restart", "pl", "testfor",
                "testforblocks", "setidletimeout", "replaceitem", "entitydata", "clone", "debug", "defaultgamemode",
                "deop", "op", "filter", "icanhasbukkit", "list", "protocol", "reload", "restart", "rl", "scoreboard",
                "seed", "spawnpoint", "spreadplayers", "stats", "trigger", "ver", "about", "achievement", "blockdata",
                "title", "help", "plugins", "time", "teleport", "tp");
        BukkitCommandFramework.INSTANCE.loadCommands("br.com.aspenmc.bukkit");
        BukkitCommandFramework.INSTANCE.registerHelp();
    }

    @Override
    public void broadcast(String... messages) {
        for (String message : messages) {
            Bukkit.broadcastMessage(message);
        }
    }

    @Override
    public void broadcast(TextComponent... components) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(components);
        }
    }

    @Override
    public void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(BukkitCommon.getInstance(), runnable);
    }

    @Override
    public void runAsyncTimer(Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(BukkitCommon.getInstance(), runnable, delay, period);
    }

    @Override
    public void runAsyncLater(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(BukkitCommon.getInstance(), runnable, delay);
    }

    @Override
    public void runSync(Runnable runnable) {
        Bukkit.getScheduler().runTask(BukkitCommon.getInstance(), runnable);
    }

    @Override
    public void runLater(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(BukkitCommon.getInstance(), runnable, delay);
    }

    @Override
    public void runTimer(Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().runTaskTimer(BukkitCommon.getInstance(), runnable, delay, period);
    }

    @Override
    public String getNameById(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        return player == null ? null : player.getName();
    }

    @Override
    public UUID getUniqueId(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        return player == null ? null : player.getUniqueId();
    }

    public int getPing(Player player) {
        return ((CraftPlayer) player).getHandle().ping;
    }

    public ProtocolVersion getProtocolVersion(Player player) {
        return ProtocolVersion.getById(ProtocolLibrary.getProtocolManager().getProtocolVersion(player));
    }
}
