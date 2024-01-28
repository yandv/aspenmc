package br.com.aspenmc;

import br.com.aspenmc.backend.Credentials;
import br.com.aspenmc.backend.data.*;
import br.com.aspenmc.backend.data.mongo.*;
import br.com.aspenmc.backend.data.redis.RedisConnectionService;
import br.com.aspenmc.backend.data.redis.RedisGeoipService;
import br.com.aspenmc.backend.data.redis.RedisServerService;
import br.com.aspenmc.backend.data.redis.RedisSkinService;
import br.com.aspenmc.backend.type.MongoConnection;
import br.com.aspenmc.backend.type.RedisConnection;
import br.com.aspenmc.entity.sender.Sender;
import br.com.aspenmc.entity.sender.member.Skin;
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

    private final UUIDFetcher uuidFetcher = new UUIDFetcher();
    @Setter
    private ClanManager clanManager;
    @Setter
    private LanguageManager languageManager;
    @Setter
    private MemberManager memberManager;
    @Setter
    private PacketManager packetManager;
    @Setter
    private PermissionManager permissionManager;
    @Setter
    private ServerManager serverManager;

    @Setter
    private ClanService clanService;

    @Setter
    private ConnectionData connectionData;

    @Setter
    private GamerService gamerService;

    @Setter
    private GeoipService geoipService;

    @Setter
    private PermissionService permissionService;

    @Setter
    private PunishService punishService;

    @Setter
    private MemberService memberService;

    @Setter
    private ServerService serverService;

    @Setter
    private SkinService skinService;

    @Setter
    private StatusService statusService;
    @Setter
    private StatusManager statusManager;

    @Setter
    private boolean serverLog = true;

    @Setter
    private boolean serverLogPackets = false;

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

        this.clanManager = new ClanManager();
        this.languageManager = new LanguageManager();
        this.memberManager = new MemberManager();
        this.packetManager = new PacketManager();
        this.permissionManager = new PermissionManager();
        this.serverManager = new ServerManager();
        this.statusManager = new StatusManager();
    }

    public void startConnection(Credentials mongoCredentials, Credentials redisCredentials) {
//        new Credentials("127.0.0.1", "", "", "aspenmc", 27017)
//        new Credentials("localhost", "", "", "", 6379)
        mongoConnection = new MongoConnection(mongoCredentials);
        redisConnection = new RedisConnection(redisCredentials);

        mongoConnection.createConnection();
        redisConnection.createConnection();

        setClanService(new MongoClanService(mongoConnection));
        setConnectionData(new RedisConnectionService());
        setGamerService(new MongoGamerService());
        setGeoipService(new RedisGeoipService());
        setMemberService(new MongoMemberService(mongoConnection));
        setPermissionService(new MongoPermissionService(mongoConnection));
        setPunishService(new MongoPunishService(mongoConnection));
        setServerService(new RedisServerService());
        setSkinService(new RedisSkinService());
        setStatusService(new MongoStatusService(mongoConnection));

        permissionService.retrieveAllGroups().forEach(group -> permissionManager.loadGroup(group));
        permissionService.retrieveAllTags().forEach(tag -> permissionManager.loadTag(tag));

        defaultSkin = skinService.loadData(CommonConst.DEFAULT_SKIN_NAME)
                                 .orElse(new Skin(CommonConst.DEFAULT_SKIN_NAME, CommonConst.CONSOLE_ID,
                                                  CommonConst.DEFAULT_SKIN_VALUE, CommonConst.SIGNATURE));
    }

    public void debug(String message) {
        logger.log(Level.INFO, "[DEBUG] " + message);
    }

    public void loadServers() {
        for (Map.Entry<String, ProxiedServer> entry : getServerService().retrieveServerByType(ServerType.values())
                                                                        .entrySet()) {
            tryAddServer(entry.getValue(), 1);
        }
    }

    private void tryAddServer(ProxiedServer server, int attemp) {
        packetManager.waitPacket(KeepAliveResponse.class,
                                 packetManager.sendPacket(new KeepAliveRequest(server.getServerId())), 3000L,
                                 response -> {
                                     if (response != null) {
                                         serverManager.addActiveServer(server);
                                         debug("The server " + server.getServerId() + " (" + server.getServerType() +
                                                       " - " + server.getOnlinePlayers() + "/" +
                                                       server.getMaxPlayers() + ") has been loaded!");
                                         return;
                                     }

                                     if (serverManager.hasServer(server.getServerId())) return;

                                     if (attemp + 1 > 3) {
                                         logger.log(Level.WARNING, "The server " + server.getServerId() +
                                                 " didn't respond to the keep alive request, stopping the connection." +
                                                 "..");
                                         serverService.stopServer(server.getServerId(), server.getServerType());
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
        skinService.save(defaultSkin);
    }

    public UUID getMojangId(String userName) {
        return uuidFetcher.getUniqueId(userName, true);
    }
}
