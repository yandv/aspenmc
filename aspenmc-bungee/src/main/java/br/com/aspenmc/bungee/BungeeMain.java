package br.com.aspenmc.bungee;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlatform;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.backend.Credentials;
import br.com.aspenmc.backend.type.RedisConnection;
import br.com.aspenmc.bungee.command.BungeeCommandFramework;
import br.com.aspenmc.bungee.entity.BungeeConsoleSender;
import br.com.aspenmc.bungee.entity.BungeeMember;
import br.com.aspenmc.bungee.event.PlayerChangedGroupEvent;
import br.com.aspenmc.bungee.listener.ChatListener;
import br.com.aspenmc.bungee.listener.LoginListener;
import br.com.aspenmc.bungee.listener.MemberListener;
import br.com.aspenmc.bungee.listener.ServerListener;
import br.com.aspenmc.bungee.manager.BungeeServerManager;
import br.com.aspenmc.bungee.manager.MotdManager;
import br.com.aspenmc.bungee.networking.BungeeCordPubSub;
import br.com.aspenmc.bungee.utils.PlayerAPI;
import br.com.aspenmc.command.CommandFramework;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.Sender;
import br.com.aspenmc.packet.type.discord.DiscordStaffMessage;
import br.com.aspenmc.packet.type.discord.MessageRequest;
import br.com.aspenmc.packet.type.discord.ServerStaffMessage;
import br.com.aspenmc.packet.type.member.MemberGroupChange;
import br.com.aspenmc.packet.type.member.MemberPunishRequest;
import br.com.aspenmc.packet.type.member.skin.SkinChangeRequest;
import br.com.aspenmc.packet.type.member.skin.SkinChangeResponse;
import br.com.aspenmc.packet.type.server.command.BungeeCommandRequest;
import br.com.aspenmc.packet.type.server.command.BungeeCommandResponse;
import br.com.aspenmc.punish.Punish;
import br.com.aspenmc.punish.PunishType;
import br.com.aspenmc.server.ServerType;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
public class BungeeMain extends Plugin implements CommonPlatform {

    @Getter
    private static BungeeMain instance;

    private CommonPlugin plugin;

    private Configuration config;

    private MotdManager motdManager;

    private boolean maintenance;
    private long maintenanceTime;
    private Set<UUID> maintenanceWhitelist;

    @Override
    public void onLoad() {
        instance = this;

        plugin = new CommonPlugin(this, getLogger());
        plugin.setConsoleSender(new BungeeConsoleSender());

        loadConfiguration();

        plugin.setServerId(getConfig().getString("serverId", "proxy.aspenmc.com.br"));
        plugin.setServerType(ServerType.BUNGEECORD);
        plugin.setServerManager(new BungeeServerManager());

        plugin.startConnection(new Credentials(getConfig().getString("mongodb.hostname", "127.0.0.1"), "", "",
                        getConfig().getString("mongodb.database", "aspenmc"), 6379),
                new Credentials(getConfig().getString("redis.hostname", "localhost"), "", "", "", 6379));
        super.onLoad();
    }

    @Override
    public void onEnable() {
        plugin.loadServers();
        plugin.getPluginPlatform().runAsync(
                new RedisConnection.PubSubListener(plugin.getRedisConnection(), new BungeeCordPubSub(),
                        CommonConst.SERVER_PACKET_CHANNEL));
        motdManager = new MotdManager();

        registerListeners();
        registerCommands();
        registerPacketHandler();

        ProxyServer.getInstance().getServers().remove("lobby");

        plugin.getPacketManager().sendPacket(new BungeeCommandResponse(
                BungeeCommandFramework.INSTANCE.getCommandMap().values().stream().map(entry -> {
                    CommandFramework.Command command = entry.getKey().getAnnotation(CommandFramework.Command.class);

                    return new BungeeCommandResponse.NormalCommand(command.name(), command.aliases(),
                            command.permission());
                }).toArray(BungeeCommandResponse.NormalCommand[]::new)));
        super.onEnable();
    }

    @Override
    public void onDisable() {

        super.onDisable();
    }

    private void loadConfiguration() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }

            File configFile = new File(getDataFolder(), "config.yml");

            if (!configFile.exists()) {
                try {
                    configFile.createNewFile();
                    try (InputStream is = getResourceAsStream("config.yml");
                            OutputStream os = Files.newOutputStream(configFile.toPath())) {
                        ByteStreams.copy(is, os);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Unable to create configuration file", e);
                }
            }

            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        maintenance = getConfig().getBoolean("maintenance", false);
        maintenanceTime = getConfig().getLong("maintenanceTime", -1L);
        maintenanceWhitelist = getConfig().contains("maintenanceWhitelist") ?
                getConfig().getStringList("maintenanceWhitelist").stream().map(UUID::fromString)
                           .collect(Collectors.toSet()) : new HashSet<>();
    }

    private void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class)
                                 .save(config, new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerPacketHandler() {
        plugin.getPacketManager().onEnable();

        plugin.setServerLogPackets(true);

        plugin.getPacketManager().registerHandler(MemberGroupChange.class, packet -> {
            plugin.getMemberManager().getMemberById(packet.getPlayerId(), BungeeMember.class).ifPresent(
                    member -> ProxyServer.getInstance().getPluginManager().callEvent(
                            new PlayerChangedGroupEvent(member, packet.getGroupName(), packet.getExpiresAt(),
                                    packet.getDuration(),
                                    PlayerChangedGroupEvent.GroupAction.valueOf(packet.getGroupAction().name()))));
        });

        plugin.getPacketManager().registerHandler(BungeeCommandRequest.class, request -> {
            plugin.getPacketManager().sendPacket(new BungeeCommandResponse(
                    BungeeCommandFramework.INSTANCE.getCommandMap().values().stream().map(entry -> {
                        CommandFramework.Command command = entry.getKey().getAnnotation(CommandFramework.Command.class);

                        return new BungeeCommandResponse.NormalCommand(command.name(), command.aliases(),
                                command.permission());
                    }).toArray(BungeeCommandResponse.NormalCommand[]::new)).id(request.getId())
                                                                           .server(request.getSource()));
        });

        plugin.getPacketManager().registerHandler(SkinChangeRequest.class, request -> {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(request.getPlayerId());

            if (player == null) {
                plugin.getPacketManager().sendPacket(
                        new SkinChangeResponse(request.getPlayerId(), SkinChangeResponse.SkinResult.PLAYER_NOT_FOUND,
                                "player-not-found").id(request.getId()).server(request.getSource()));
                return;
            }

            try {
                PlayerAPI.changePlayerSkin(player.getPendingConnection(), request.getSkin());
                plugin.getPacketManager().sendPacket(
                        new SkinChangeResponse(request.getPlayerId(), SkinChangeResponse.SkinResult.SUCCESS, "")
                                .id(request.getId()).server(request.getSource()));
            } catch (NoSuchFieldException | IllegalAccessException exception) {
                plugin.getPacketManager().sendPacket(
                        new SkinChangeResponse(request.getPlayerId(), SkinChangeResponse.SkinResult.UNKNOWN_ERROR,
                                exception.getMessage()).id(request.getId()).server(request.getSource()));
                throw new RuntimeException(exception);
            }
        });

        plugin.getPacketManager().registerHandler(MemberPunishRequest.class, packet -> {
            Member target = plugin.getMemberManager().getOrLoadById(packet.getUniqueId()).orElse(null);

            if (target == null) {
                CommonPlugin.getInstance().getLogger().warning("Target not found for punish " + packet.getUniqueId());
                return;
            }

            Sender punisher = packet.getPunisherId().equals(CommonConst.CONSOLE_ID) ?
                    CommonPlugin.getInstance().getConsoleSender() :
                    plugin.getMemberManager().getOrLoadById(packet.getPunisherId()).orElse(null);

            if (punisher == null) {
                CommonPlugin.getInstance().getLogger()
                            .warning("Punisher not found for punish " + packet.getPunisherId());
                return;
            }


            Punish punish = CommonPlugin.getInstance().getPunishService()
                                        .createPunish(target, punisher, packet.getPunishType(), packet.getReason(),
                                                packet.getExpiresAt()).join();

            target.loadConfiguration();
            target.getPunishConfiguration().punish(punish);

            MessageRequest.sendPlayerPunish(punish);

            String punishMessage = punish.getPunishMessage(target.getLanguage());

            if (packet.getPunishType() == PunishType.BAN) {
                if (target instanceof BungeeMember) {
                    BungeeMember bungeeMember = (BungeeMember) target;

                    bungeeMember.getProxiedPlayer().disconnect(punishMessage);
                }
            } else {
                target.sendMessage(punishMessage);
            }
        });

        plugin.getPacketManager().registerHandler(DiscordStaffMessage.class,
                packet -> sendStaffChatMessage(packet.getDiscriminator(), packet.getMessage()));
    }

    public void registerListeners() {
        getProxy().getPluginManager().registerListener(this, new ChatListener());
        getProxy().getPluginManager().registerListener(this, new MemberListener());
        getProxy().getPluginManager().registerListener(this, new LoginListener());
        getProxy().getPluginManager().registerListener(this, new ServerListener());
    }

    public void registerCommands() {
        BungeeCommandFramework.INSTANCE.loadCommands("br.com.aspenmc.bungee.command.register");
    }

    public void setMaintenance(boolean maintenance, long time) {
        this.maintenance = maintenance;
        this.maintenanceTime = -1L;

        getConfig().set("maintenance", maintenance);
        getConfig().set("maintenanceTime", time);
        saveConfig();
    }

    public boolean isMaintenance() {
        return maintenance && (maintenanceTime == -1L || maintenanceTime > System.currentTimeMillis());
    }

    public void setMaintenance(boolean maintenance) {
        setMaintenance(maintenance, -1L);
    }

    @Override
    public void broadcast(String... messages) {
        for (String message : messages) {
            ProxyServer.getInstance().broadcast(message);
        }
    }

    @Override
    public void broadcast(TextComponent... components) {
        ProxyServer.getInstance().broadcast(components);
    }

    @Override
    public String getNameById(UUID playerId) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerId);
        return player == null ? null : player.getName();
    }

    @Override
    public UUID getUniqueId(String playerName) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);
        return player == null ? null : player.getUniqueId();
    }

    @Override
    public void runAsync(Runnable runnable) {
        ProxyServer.getInstance().getScheduler().runAsync(BungeeMain.getInstance(), runnable);
    }

    @Override
    public void runAsyncTimer(Runnable runnable, long delay, long period) {
        ProxyServer.getInstance().getScheduler()
                   .schedule(BungeeMain.getInstance(), runnable, (long) ((delay / 20.0D) * 1000),
                           (long) ((period / 20.0D) * 1000), TimeUnit.MILLISECONDS);
    }

    @Override
    public void runAsyncLater(Runnable runnable, long delay) {
        ProxyServer.getInstance().getScheduler()
                   .schedule(BungeeMain.getInstance(), runnable, (long) ((delay / 20.0D) * 1000),
                           TimeUnit.MILLISECONDS);
    }

    @Override
    public void runSync(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void runLater(Runnable runnable, long delay) {
        ProxyServer.getInstance().getScheduler()
                   .schedule(BungeeMain.getInstance(), runnable, (long) ((delay / 20.0D) * 1000),
                           TimeUnit.MILLISECONDS);
    }

    @Override
    public void runTimer(Runnable runnable, long delay, long period) {
        ProxyServer.getInstance().getScheduler()
                   .schedule(BungeeMain.getInstance(), runnable, (long) ((delay / 20.0D) * 1000),
                           (long) ((period / 20.0D) * 1000), TimeUnit.MILLISECONDS);
    }

    public void sendStaffChatMessage(String discriminator, String message) {
        plugin.getMemberManager().getMembers().stream().filter(member -> member.hasPermission("command.staffchat"))
              .filter(member -> member.getPreferencesConfiguration().isSeeingStaffChatEnabled())
              .filter(member -> member.getLoginConfiguration().isLogged()).forEach(member -> member.sendMessage(
                      "§6Staff> §7" + discriminator + "§7: §f" + ChatColor.translateAlternateColorCodes('&', message)));
    }

    public void sendStaffChatMessage(Member sender, String message, boolean discord) {
        plugin.getMemberManager().getMembers().stream().filter(member -> member.hasPermission("command.staffchat"))
              .filter(member -> member.getPreferencesConfiguration().isSeeingStaffChatEnabled())
              .filter(member -> member.getLoginConfiguration().isLogged()).forEach(member -> member.sendMessage(
                      "§6Staff> " + sender.getDefaultTag().getRealPrefix() + sender.getName() + "§7: §f" +
                              ChatColor.translateAlternateColorCodes('&', message)));
        plugin.getPacketManager().sendPacket(
                new ServerStaffMessage(sender.getUniqueId(), sender.getName(), sender.getServerGroup().getGroupName(),
                        message));
    }

    public long getAveragePing() {
        return ProxyServer.getInstance().getPlayers().stream().mapToInt(ProxiedPlayer::getPing).sum() /
                Math.max(ProxyServer.getInstance().getOnlineCount(), 1);
    }
}
