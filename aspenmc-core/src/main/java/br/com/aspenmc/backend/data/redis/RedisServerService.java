package br.com.aspenmc.backend.data.redis;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.backend.data.ServerService;
import br.com.aspenmc.packet.Packet;
import br.com.aspenmc.packet.type.server.ServerStart;
import br.com.aspenmc.packet.type.server.ServerStop;
import br.com.aspenmc.packet.type.server.ServerUpdate;
import br.com.aspenmc.server.ProxiedServer;
import br.com.aspenmc.server.ServerType;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.*;
import java.util.stream.Collectors;

public class RedisServerService implements ServerService {

    private static final String REDIS_SERVER_PREFIX = "aspenmc-server:";

    @Override
    public void startServer(int maxPlayers) {
        startServer(new ProxiedServer(CommonPlugin.getInstance().getServerAddress(),
                CommonPlugin.getInstance().getServerPort(), CommonPlugin.getInstance().getServerId(),
                CommonPlugin.getInstance().getServerType(), new HashSet<>(), maxPlayers, true), maxPlayers);
    }

    @Override
    public void startServer(ProxiedServer server, int maxPlayers) {
        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            Pipeline pipe = jedis.pipelined();
            pipe.sadd(REDIS_SERVER_PREFIX + "type:" + server.getServerType().getName().toLowerCase(),
                    server.getServerId());

            Map<String, String> map = new HashMap<>();
            map.put("type", server.getServerType().getName().toLowerCase());
            map.put("maxplayers", Integer.toString(server.getMaxPlayers()));
            map.put("joinenabled", Boolean.toString(server.isJoinEnabled()));
            map.put("address", server.getServerAddress());
            map.put("port", Integer.toString(server.getServerPort()));
            map.put("map", server.getMapName());
            map.put("time", "" + server.getTime());
            map.put("state", "" + server.getState().name().toLowerCase());
            map.put("starttime", Long.toString(System.currentTimeMillis()));

            pipe.hmset(REDIS_SERVER_PREFIX + server.getServerId(), map);
            pipe.del(REDIS_SERVER_PREFIX + server.getServerId() + ":players");
            pipe.sync();
        }

        sendPacket(new ServerStart(server));
    }

    @Override
    public void stopServer() {
        stopServer(CommonPlugin.getInstance().getServerId(), CommonPlugin.getInstance().getServerType());
    }

    @Override
    public void stopServer(String serverId, ServerType serverType) {
        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            Pipeline pipe = jedis.pipelined();
            pipe.srem(REDIS_SERVER_PREFIX + "type:" + serverType.getName().toLowerCase(), serverId);
            pipe.del(REDIS_SERVER_PREFIX + serverId);
            pipe.del(REDIS_SERVER_PREFIX + serverId + ":players");
            pipe.sync();
        }

        sendPacket(new ServerStop(serverId, serverType));
    }

    @Override
    public void setMaxPlayers(int maxPlayers) {
        setMaxPlayers(CommonPlugin.getInstance().getServerId(), CommonPlugin.getInstance().getServerType(), maxPlayers);
    }

    @Override
    public void setMaxPlayers(String serverId, ServerType serverType, int maxPlayers) {
        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            Pipeline pipe = jedis.pipelined();
            pipe.hset(REDIS_SERVER_PREFIX + serverId, "maxplayers", Integer.toString(maxPlayers));
            pipe.sync();
        }

        sendPacket(new ServerUpdate(serverId, serverType, maxPlayers));
    }

    @Override
    public void joinPlayer(UUID uniqueId) {
        joinPlayer(CommonPlugin.getInstance().getServerId(), CommonPlugin.getInstance().getServerType(), uniqueId);
    }

    @Override
    public void joinPlayer(String serverId, ServerType serverType, UUID uniqueId) {
        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            Pipeline pipe = jedis.pipelined();
            pipe.sadd(REDIS_SERVER_PREFIX + serverId + ":players", uniqueId.toString());
            pipe.sync();
        }

        sendPacket(new ServerUpdate(serverId, serverType, uniqueId, ServerUpdate.UpdateType.JOIN));
    }

    @Override
    public void leavePlayer(UUID uniqueId) {
        leavePlayer(CommonPlugin.getInstance().getServerId(), CommonPlugin.getInstance().getServerType(), uniqueId);
    }

    @Override
    public void leavePlayer(String serverId, ServerType serverType, UUID uniqueId) {
        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            Pipeline pipe = jedis.pipelined();
            pipe.sadd(REDIS_SERVER_PREFIX + serverId + ":players", uniqueId.toString());
            pipe.sync();
        }

        sendPacket(new ServerUpdate(serverId, serverType, uniqueId, ServerUpdate.UpdateType.LEAVE));
    }

    @Override
    public Set<UUID> getOnlinePlayers(String serverId) {
        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            return jedis.smembers(REDIS_SERVER_PREFIX + serverId + ":players").stream().map(UUID::fromString)
                        .collect(Collectors.toSet());
        }
    }

    @Override
    public Map<String, ProxiedServer> retrieveServerByType(ServerType... serversType) {
        Map<String, ProxiedServer> map = new HashMap<>();

        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            String[] str = new String[serversType.length];

            for (int i = 0; i < serversType.length; i++) {
                str[i] = REDIS_SERVER_PREFIX + "type:" + ServerType.values()[i].name().toLowerCase();
            }

            for (String serverId : jedis.sunion(str)) {
                map.put(serverId, createProxiedServer(jedis, serverId));
            }
        }

        return map;
    }

    @Override
    public Map<String, ProxiedServer> retrieveServerById(String... serversId) {
        Map<String, ProxiedServer> map = new HashMap<>();

        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            for (String serverId : serversId) {
                map.put(serverId, createProxiedServer(jedis, serverId));
            }
        }

        return map;
    }

    @Override
    public <T extends Packet> T sendPacket(T packet) {
        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            jedis.publish(CommonConst.SERVER_PACKET_CHANNEL, CommonConst.GSON.toJson(packet));
        }

        return packet;
    }


    @Override
    public void sendPacket(Packet... packets) {
        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            Pipeline pipe = jedis.pipelined();
            for (Packet packet : packets) {
                pipe.publish(CommonConst.SERVER_PACKET_CHANNEL, CommonConst.GSON.toJson(packet));
            }
            pipe.sync();
        }
    }

    private ProxiedServer createProxiedServer(Jedis jedis, String serverId) {
        Map<String, String> jedisMap = jedis.hgetAll(REDIS_SERVER_PREFIX + serverId);

        ServerType serverType = ServerType.getByName(jedisMap.get("type"));
        int maxPlayers = Integer.parseInt(jedisMap.get("maxplayers"));
        boolean joinEnabled = Boolean.parseBoolean(jedisMap.get("joinenabled"));
        String serverAddress = jedisMap.get("address");
        int serverPort = Integer.parseInt(jedisMap.get("port"));
        String serverMap = jedisMap.get("map");
        int time = Integer.parseInt(jedisMap.get("time"));
        ProxiedServer.GameState state = ProxiedServer.GameState.valueOf(jedisMap.get("state").toUpperCase());
        long startTime = Long.parseLong(jedisMap.get("starttime"));
        Set<UUID> players = jedis.smembers(REDIS_SERVER_PREFIX + serverId + ":players").stream().map(UUID::fromString)
                                 .collect(Collectors.toSet());

        ProxiedServer server = new ProxiedServer(serverAddress, serverPort, serverId, serverType, players, maxPlayers,
                joinEnabled, state, time, serverMap);

        server.setStartTime(startTime);

        server.setPlayers(players);
        return server;
    }
}
