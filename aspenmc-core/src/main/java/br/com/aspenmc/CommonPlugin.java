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
import br.com.aspenmc.manager.MemberManager;
import br.com.aspenmc.manager.PacketManager;
import br.com.aspenmc.manager.PermissionManager;
import br.com.aspenmc.manager.ServerManager;
import br.com.aspenmc.packet.type.server.keepalive.KeepAliveRequest;
import br.com.aspenmc.packet.type.server.keepalive.KeepAliveResponse;
import br.com.aspenmc.server.ProxiedServer;
import br.com.aspenmc.server.ServerType;
import br.com.aspenmc.utils.mojang.UUIDFetcher;
import lombok.Getter;
import lombok.Setter;
import org.intellij.lang.annotations.Language;

import java.util.ArrayList;
import java.util.List;
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
    private MemberManager memberManager = new MemberManager();

    @Setter
    private PacketManager packetManager = new PacketManager();

    @Setter
    private PermissionManager permissionManager = new PermissionManager();

    @Setter
    private ServerManager serverManager = new ServerManager();

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

    private UUIDFetcher uuidFetcher = new UUIDFetcher();

    @Setter
    private boolean serverLog = true;

    @Setter
    private boolean piratePlayersEnabled = true;


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

        setGamerData(new MongoGamerData());
        setMemberData(new MongoMemberData(mongoConnection));
        setPermissionData(new MongoPermissionData(mongoConnection));
        setPunishData(new PunishDataImpl(mongoConnection));
        setServerData(new RedisServerData());
        setSkinData(new RedisSkinData());

        permissionData.retrieveAllGroups().forEach(group -> permissionManager.loadGroup(group));
        permissionData.retrieveAllTags().forEach(tag -> permissionManager.loadTag(tag));

        defaultSkin = skinData.loadData("Sem pele").orElse(null);

        if (defaultSkin == null) {
            setDefaultSkin(new Skin("Sem pele", CommonConst.CONSOLE_ID,
                    "ewogICJ0aW1lc3RhbXAiIDogMTY3NzI0NTI0OTE5MCwKICAicHJvZmlsZUlkIiA6ICI4NzQ3ODgyNjc2NzI0OTk1ODU1ODMwN2FiMWI3ZDRjZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUZXN0ZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yYzFjYzE5YzYzZDM4MmI3ZTI5MzhkZWE4NGZmZmYzODYxMmJkM2IwNjM3NzY4NzkwZTZkNTJkNzEwNDZhNGIyIgogICAgfQogIH0KfQ==",
                    "qBgm2nqCyotJW0obDPws0w0iFjlyAK1kEyc3bAPukNPEu6mhdXi2VtVhSeQoW80DlTCn8Svcxzu2/8PGxYbT5/DkvHfA" +
                            "/yqgrgN6r3rSktC/AQMw6QxX/+h0r76ySO5VbcwPyhqekBcyu" +
                            "+EnuvOJ8nwdUKdVdZaHN4BYiHBtaKCwkG6GuhfrsDnxC5sjHa1GxkY9w9Wb83Zwn1lW" +
                            "+qFI8leeobYhPcmO9Y6a2B0u76yc55UoeHdxuuehtweeAKI3pKaCO0ckMBRMV4qhPbvWIFNJhNDTfjrR4JWwK4" +
                            "+tmq" +
                            "//3C470Cz4NQg0rNpe5yCBhxctn3yBJrs5M0fQKH559UdQ5wmdYufMtHy8HIa16jqn58UhJxN4P0A8KNwrL8qIOe67nCny+aATOWBo/IAywA4rITFsTAVCP5ViJyNOszEi4oj+/xbdUoDpqeLHJGJmef+PoP5oSvNfha/ZfTYXD+b4odN5SDema7xS/JLl774zDJCBPH47Y8fkY5tYdM/gk7lODMZHCRDCVErhXQqI4Bu9fY5z4Hnl8nUqQjKAn6UNjRA0xkxtL9SUPqD2l+OaUay9rJhcoyLNPr55v8P9qbHi1bg7zlcaXFMBcPiUdG8karSl8fhyfQ27AF94lF5L3kSH5yxa+ksOYYrXImRvDIsiFs45sqvFF0TnI8NQRYU="));
        }
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
                packetManager.sendPacket(new KeepAliveRequest(server.getServerId())), 500L, response -> {
                    if (response != null) {
                        serverManager.addActiveServer(server);
                        debug("The server " + server.getServerId() + " (" + server.getServerType() + " - " +
                                server.getOnlinePlayers() + "/" + server.getMaxPlayers() + ") has been loaded!");
                        return;
                    }

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
        skinData.save(defaultSkin, 999999);
    }

    public UUID getMojangId(String userName) {
        return uuidFetcher.getUniqueId(userName, true);
    }
}
