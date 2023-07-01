package br.com.aspenmc.bukkit;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.backend.type.RedisConnection;
import br.com.aspenmc.bukkit.command.BukkitCommandFramework;
import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.player.group.PlayerChangedGroupEvent;
import br.com.aspenmc.bukkit.event.server.ServerUpdateEvent;
import br.com.aspenmc.bukkit.listener.*;
import br.com.aspenmc.bukkit.manager.CharacterManager;
import br.com.aspenmc.bukkit.manager.HologramManager;
import br.com.aspenmc.bukkit.manager.VanishManager;
import br.com.aspenmc.bukkit.networking.BukkitPubSub;
import br.com.aspenmc.bukkit.permission.regex.RegexPermissions;
import br.com.aspenmc.bukkit.entity.BukkitConsoleSender;
import br.com.aspenmc.bukkit.scheduler.UpdateScheduler;
import br.com.aspenmc.entity.member.gamer.Gamer;
import br.com.aspenmc.packet.type.member.MemberGroupChange;
import br.com.aspenmc.packet.type.member.server.ServerConnectRequest;
import br.com.aspenmc.packet.type.server.ServerUpdate;
import br.com.aspenmc.packet.type.server.command.BungeeCommandRequest;
import br.com.aspenmc.packet.type.server.command.BungeeCommandResponse;
import br.com.aspenmc.server.ProxiedServer;
import br.com.aspenmc.server.ServerType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

@Getter
public abstract class BukkitCommon extends JavaPlugin {

    @Getter
    private static BukkitCommon instance;

    protected CommonPlugin plugin = new CommonPlugin(new BukkitPlatform(), getLogger());

    protected RegexPermissions regexPerms;

    protected CharacterManager characterManager;
    protected HologramManager hologramManager;
    protected VanishManager vanishManager;

    private ProxiedServer.GameState state;
    private int time;
    @Setter
    private boolean timeEnabled;
    private String mapName;

    @Setter
    private boolean tagControl = true;

    @Setter
    private Set<Map.Entry<String, Class<? extends Gamer>>> gamerList;

    @Setter
    private boolean saveGamers = true;

    @Override
    public void onLoad() {
        instance = this;

        saveDefaultConfig();

        plugin.setConsoleSender(new BukkitConsoleSender());

        plugin.setServerAddress(Bukkit.getIp());
        plugin.setServerPort(Bukkit.getPort());

        plugin.setServerId(getConfig().getString("serverId", "unnamed-server"));
        plugin.setServerType(Optional.ofNullable(ServerType.getByName(getConfig().getString("serverType")))
                                     .orElse(ServerType.LOBBY));

        plugin.startConnection();
        plugin.getServerData().startServer(Bukkit.getMaxPlayers());

        plugin.debug("Starting the server " + plugin.getServerId() + " (" + plugin.getServerType().name() + ").");

        super.onLoad();
        onCompleteLoad();
    }

    public void onCompleteLoad() {

    }

    @Override
    public void onEnable() {
        regexPerms = new RegexPermissions();

        Bukkit.getScheduler().runTaskAsynchronously(this,
                                                    new RedisConnection.PubSubListener(plugin.getRedisConnection(),
                                                                                       new BukkitPubSub(),
                                                                                       CommonConst.SERVER_PACKET_CHANNEL));

        characterManager = new CharacterManager();
        hologramManager = new HologramManager();
        vanishManager = new VanishManager();

        if (plugin.getPermissionManager().getTags().isEmpty()) {
            setTagControl(false);
        }

        gamerList = new HashSet<>();

        registerPacketHandlers();
        registerListeners();
        plugin.getPluginPlatform().runLater(this::registerCommands, 7L);

        getServer().getScheduler().runTaskTimer(this, new UpdateScheduler(), 1, 1);

        if (plugin.isServerLog()) {
            plugin.loadServers();
        }

        plugin.debug("Started the server " + plugin.getServerId() + " (" + plugin.getServerType().name() + ").");

        super.onEnable();
        onCompleteStart();
    }

    public void onCompleteStart() {

    }

    @Override
    public void onDisable() {

        plugin.getServerData().stopServer();
        plugin.debug("Stopped the server " + plugin.getServerId() + " (" + plugin.getServerType().name() + ").");

        super.onDisable();
    }

    public void updateState(ProxiedServer.GameState state, int time) {
        this.state = state;
        this.time = time;
        CommonPlugin.getInstance().getPacketManager()
                    .sendPacketAsync(new ServerUpdate(CommonPlugin.getInstance().getServerId(), state, time, mapName));
    }

    public void updateState(ProxiedServer.GameState state, int time, String mapName) {
        this.state = state;
        this.time = time;
        this.mapName = mapName;
        CommonPlugin.getInstance().getPacketManager()
                    .sendPacketAsync(new ServerUpdate(CommonPlugin.getInstance().getServerId(), state, time, mapName));
    }

    public void updateState(ProxiedServer.GameState state) {
        this.state = state;
        CommonPlugin.getInstance().getPacketManager()
                    .sendPacketAsync(new ServerUpdate(CommonPlugin.getInstance().getServerId(), state, time, mapName));
    }

    public void updateTime(int time) {
        this.time = time;
        CommonPlugin.getInstance().getPacketManager()
                    .sendPacketAsync(new ServerUpdate(CommonPlugin.getInstance().getServerId(), state, time, mapName));
    }

    public void updateMapName(String mapName) {
        this.mapName = mapName;
        CommonPlugin.getInstance().getPacketManager()
                    .sendPacketAsync(new ServerUpdate(CommonPlugin.getInstance().getServerId(), state, time, mapName));
    }

    public void loadGamer(String gamerId, Class<? extends Gamer> gamerClass) {
        gamerList.add(new AbstractMap.SimpleEntry<>(gamerId, gamerClass));
    }

    public void sendPlayerToServer(Player player, String... serversIds) {
        plugin.getServerData().sendPacket(new ServerConnectRequest(player.getUniqueId(), serversIds));
    }

    public void sendPlayerToServer(Player player, ServerType... types) {
        plugin.getServerData().sendPacket(new ServerConnectRequest(player.getUniqueId(), types));
    }

    public void registerPacketHandlers() {
        plugin.getPacketManager().onEnable();

        plugin.getPacketManager().registerHandler(MemberGroupChange.class, packet -> {
            plugin.getMemberManager().getMemberById(packet.getPlayerId(), BukkitMember.class).ifPresent(
                    member -> Bukkit.getPluginManager().callEvent(
                            new PlayerChangedGroupEvent(member, packet.getGroupName(), packet.getExpiresAt(),
                                                        packet.getDuration(),
                                                        PlayerChangedGroupEvent.GroupAction.valueOf(
                                                                packet.getGroupAction().name()))));
        });

        plugin.getPacketManager().registerHandler(BungeeCommandResponse.class, packet -> {
            for (BungeeCommandResponse.NormalCommand command : packet.getCommands()) {
                BukkitCommandFramework.INSTANCE.getKnownCommands().put(command.getName(),
                                                                       BukkitCommandFramework.INSTANCE.createCommand(
                                                                               command.getName(), command.getName(),
                                                                               command.getPermission()));

                for (String alias : command.getAliases()) {
                    BukkitCommandFramework.INSTANCE.getKnownCommands().put(alias,
                                                                           BukkitCommandFramework.INSTANCE.createCommand(
                                                                                   alias, command.getName(),
                                                                                   command.getPermission()));
                }
            }
        });

        plugin.getPacketManager().registerHandler(ServerUpdate.class, packet -> Bukkit.getPluginManager().callEvent(
                new ServerUpdateEvent(packet)));

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
        Bukkit.getPluginManager().registerEvents(new TagListener(), this);
        Bukkit.getPluginManager().registerEvents(new VanishListener(), this);
    }

    public void registerCommands() {
        BukkitCommandFramework.INSTANCE.unregisterCommands("me", "tellraw", "?", "whitelist", "gamemode", "clear",
                                                           "tps", "ban", "ban-ip", "banlist", "pardon", "pardon-ip",
                                                           "stop", "restart");
        BukkitCommandFramework.INSTANCE.loadCommands("br.com.aspenmc.bukkit.command.register");
    }
}
