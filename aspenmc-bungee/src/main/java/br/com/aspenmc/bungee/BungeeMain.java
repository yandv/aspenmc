package br.com.aspenmc.bungee;

import br.com.aspenmc.CommonPlatform;
import br.com.aspenmc.backend.type.RedisConnection;
import br.com.aspenmc.bungee.entity.BungeeConsoleSender;
import br.com.aspenmc.bungee.listener.MemberListener;
import br.com.aspenmc.bungee.listener.ServerListener;
import br.com.aspenmc.packet.type.member.skin.SkinChangeRequest;
import br.com.aspenmc.packet.type.member.skin.SkinChangeResponse;
import br.com.aspenmc.packet.type.server.command.BungeeCommandRequest;
import br.com.aspenmc.packet.type.server.command.BungeeCommandResponse;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bungee.command.BungeeCommandFramework;
import br.com.aspenmc.bungee.manager.BungeeServerManager;
import br.com.aspenmc.bungee.manager.MotdManager;
import br.com.aspenmc.bungee.networking.BungeeCordPubSub;
import br.com.aspenmc.bungee.utils.PlayerAPI;
import br.com.aspenmc.command.CommandFramework;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.server.ServerType;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public class BungeeMain extends Plugin implements CommonPlatform {

    @Getter
    private static BungeeMain instance;

    private CommonPlugin plugin;

    private Configuration config;

    private MotdManager motdManager;

    @Override
    public void onLoad() {
        instance = this;

        plugin = new CommonPlugin(this, getLogger());
        plugin.setConsoleSender(new BungeeConsoleSender());

        loadConfiguration();

        plugin.setServerId(getConfig().getString("serverId", "proxy.aspenmc.com.br"));
        plugin.setServerType(ServerType.BUNGEECORD);
        plugin.setServerManager(new BungeeServerManager());

        plugin.startConnection();
        super.onLoad();
    }

    @Override
    public void onEnable() {
        plugin.loadServers();
        getProxy().getScheduler().runAsync(this, new RedisConnection.PubSubListener(plugin.getRedisConnection(),
                                                                                    new BungeeCordPubSub(),
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
    }

    public void registerPacketHandler() {
        plugin.getPacketManager().onEnable();

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

            if (player == null) return;

            try {
                PlayerAPI.changePlayerSkin(player.getPendingConnection(), request.getSkin());
            } catch (NoSuchFieldException | IllegalAccessException exception) {
                plugin.getPacketManager().sendPacket(
                        new SkinChangeResponse(request.getPlayerId(), SkinChangeResponse.SkinResult.UNKNOWN_ERROR)
                                .id(request.getId()).server(request.getSource()));
                throw new RuntimeException(exception);
            }

            plugin.getPacketManager().sendPacket(
                    new SkinChangeResponse(request.getPlayerId(), SkinChangeResponse.SkinResult.SUCCESS)
                            .id(request.getId()).server(request.getSource()));
        });
    }

    public void registerListeners() {
        getProxy().getPluginManager().registerListener(this, new MemberListener());
        getProxy().getPluginManager().registerListener(this, new ServerListener());
    }

    public void registerCommands() {
        BungeeCommandFramework.INSTANCE.loadCommands("br.com.aspenmc.bungee.command.register");
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

    public void sendStaffChatMessage(Member sender, String message) {
        plugin.getMemberManager().getMembers().stream().filter(member -> member.hasPermission("command.staffchat"))
              .filter(member -> member.getPreferencesConfiguration().isSeeingStaffChatEnabled())
              .filter(member -> member.getLoginConfiguration().isLogged()).forEach(member -> member.sendMessage(
                      "§6Staff> " + sender.getDefaultTag().getRealPrefix() + sender.getName() + "§7: §f" + message));
    }
}
