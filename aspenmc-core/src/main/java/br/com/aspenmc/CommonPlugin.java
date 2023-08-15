package br.com.aspenmc;

import br.com.aspenmc.backend.Credentials;
import br.com.aspenmc.backend.data.*;
import br.com.aspenmc.backend.data.mongo.*;
import br.com.aspenmc.backend.data.redis.RedisServerData;
import br.com.aspenmc.backend.data.redis.RedisSkinData;
import br.com.aspenmc.backend.type.MongoConnection;
import br.com.aspenmc.backend.type.RedisConnection;
import br.com.aspenmc.entity.Sender;
import br.com.aspenmc.entity.member.Skin;
import br.com.aspenmc.language.Language;
import br.com.aspenmc.manager.*;
import br.com.aspenmc.packet.type.server.keepalive.KeepAliveRequest;
import br.com.aspenmc.packet.type.server.keepalive.KeepAliveResponse;
import br.com.aspenmc.server.ProxiedServer;
import br.com.aspenmc.server.ServerType;
import br.com.aspenmc.utils.mojang.UUIDFetcher;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter

public class CommonPlugin {

    @Getter
    private static CommonPlugin instance;

    private CommonPlatform pluginPlatform;

    private Logger logger;

    private MongoConnection mongoConnection;
    private RedisConnection redisConnection;

    @Setter
    private Sender consoleSender;

    @Setter
    private String serverAddress;
    @Setter
    private int serverPort;

    @Setter
    private String serverId;
    @Setter
    private ServerType serverType;

    private Skin defaultSkin;

    @Setter
    private ClanManager clanManager = new ClanManager();

    @Setter
    private LanguageManager languageManager = new LanguageManager();

    @Setter
    private MemberManager memberManager = new MemberManager();

    @Setter
    private PacketManager packetManager = new PacketManager();

    @Setter
    private PermissionManager permissionManager = new PermissionManager();

    @Setter
    private ServerManager serverManager = new ServerManager();

    @Setter
    private StatusManager statusManager = new StatusManager();

    @Setter
    private ClanData clanData;

    @Setter
    private GamerData gamerData;

    @Setter
    private PermissionData permissionData;

    @Setter
    private PunishData punishData;

    @Setter
    private MemberData memberData;

    @Setter
    private ServerData serverData;

    @Setter
    private SkinData skinData;

    @Setter
    private StatusData statusData;

    private UUIDFetcher uuidFetcher = new UUIDFetcher();

    @Setter
    private boolean serverLog = true;

    @Setter
    private boolean piratePlayersEnabled = true;

    @Setter
    private Language defaultLanguage = Language.PORTUGUESE;


    public CommonPlugin() {
        instance = this;
    }

    public CommonPlugin(CommonPlatform pluginPlatform, Logger logger) {
        this.pluginPlatform = pluginPlatform;
        this.logger = logger;
        instance = this;
    }

    public void startConnection() {
        mongoConnection = new MongoConnection(new Credentials("127.0.0.1", "", "", "aspenmc", 27017));
        redisConnection = new RedisConnection(new Credentials("localhost", "", "", "", 6379));

        mongoConnection.createConnection();
        redisConnection.createConnection();

        setClanData(new MongoClanData(mongoConnection));
        setGamerData(new MongoGamerData());
        setMemberData(new MongoMemberData(mongoConnection));
        setPermissionData(new MongoPermissionData(mongoConnection));
        setPunishData(new MongoPunishData(mongoConnection));
        setServerData(new RedisServerData());
        setSkinData(new RedisSkinData());
        setStatusData(new MongoStatusData(mongoConnection));

        permissionData.retrieveAllGroups().forEach(group -> permissionManager.loadGroup(group));
        permissionData.retrieveAllTags().forEach(tag -> permissionManager.loadTag(tag));

        defaultSkin = skinData.loadData(CommonConst.DEFAULT_SKIN_NAME)
                              .orElse(new Skin(CommonConst.DEFAULT_SKIN_NAME, CommonConst.CONSOLE_ID,
                                      CommonConst.DEFAULT_SKIN_VALUE, CommonConst.SIGNATURE));
    }

    public void debug(String message) {
        System.out.println("[DEBUG] " + message);
    }

    public void loadServers() {
        for (Map.Entry<String, ProxiedServer> entry : getServerData().retrieveServerByType(ServerType.values())
                                                                     .entrySet()) {
            tryAddServer(entry.getValue(), 1);
        }
    }

    private void tryAddServer(ProxiedServer server, int attemp) {
        packetManager.waitPacket(KeepAliveResponse.class,
                packetManager.sendPacket(new KeepAliveRequest(server.getServerId())), 3000L, response -> {
                    if (response != null) {
                        serverManager.addActiveServer(server);
                        debug("The server " + server.getServerId() + " (" + server.getServerType() + " - " +
                                server.getOnlinePlayers() + "/" + server.getMaxPlayers() + ") has been loaded!");
                        return;
                    }

                    if (serverManager.hasServer(server.getServerId())) return;

                    if (attemp + 1 > 3) {
                        logger.log(Level.WARNING, "The server " + server.getServerId() +
                                " didn't respond to the keep alive request, stopping the connection...");
                        serverData.stopServer(server.getServerId(), server.getServerType());
                        return;
                    }

                    tryAddServer(server, attemp + 1);
                });
    }

    public void stopConnection() {
        mongoConnection.closeConnection();
        redisConnection.closeConnection();
    }

    public void setDefaultSkin(Skin defaultSkin) {
        this.defaultSkin = defaultSkin;
        skinData.save(defaultSkin);
    }

    public UUID getMojangId(String userName) {
        return uuidFetcher.getUniqueId(userName, true);
    }
}
