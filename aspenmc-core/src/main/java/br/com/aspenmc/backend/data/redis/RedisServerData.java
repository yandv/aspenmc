package br.com.aspenmc.backend.data.redis;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.backend.data.ServerData;
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

public class RedisServerData implements ServerData {

    @Override
    public void startServer(int maxPlayers) {
        startServer(new ProxiedServer(CommonPlugin.getInstance().getServerAddress(),
                                      CommonPlugin.getInstance().getServerPort(),
                                      CommonPlugin.getInstance().getServerId(),
                                      CommonPlugin.getInstance().getServerType(), new HashSet<>(), maxPlayers, true),
                    maxPlayers);
    }

    @Override
    public void startServer(ProxiedServer server, int maxPlayers) {
        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            Pipeline pipe = jedis.pipelined();
            pipe.sadd("server:type:" + server.getServerType().getName().toLowerCase(), server.getServerId());
            Map<String, String> map = new HashMap<>();
            map.put("type", server.getServerType().getName().toLowerCase());
            map.put("maxplayers", Integer.toString(server.getMaxPlayers()));
            map.put("joinenabled", Boolean.toString(server.isJoinEnabled()));
            map.put("address", server.getServerAddress());
            map.put("port", Integer.toString(server.getServerPort()));
            map.put("map", "");
            map.put("time", "");
            map.put("state", "");
            map.put("starttime", Long.toString(System.currentTimeMillis()));
            pipe.hmset("server:" + server.getServerId(), map);
            pipe.del("server:" + server.getServerId() + ":players");
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
            pipe.srem("server:type:" + serverType.getName().toLowerCase(), serverId);
            pipe.del("server:" + serverId);
            pipe.del("server:" + serverId + ":players");
            pipe.sync();
        }

        sendPacket(new ServerStop(serverId));
    }

    @Override
    public void setMaxPlayers(int maxPlayers) {
        setMaxPlayers(CommonPlugin.getInstance().getServerId(), maxPlayers);
    }

    @Override
    public void setMaxPlayers(String serverId, int maxPlayers) {
        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            Pipeline pipe = jedis.pipelined();
            pipe.hset("server:" + serverId, "maxplayers", Integer.toString(maxPlayers));
            pipe.sync();
        }

        sendPacket(new ServerUpdate(serverId, maxPlayers));
    }

    @Override
    public void joinPlayer(UUID uniqueId) {
        joinPlayer(CommonPlugin.getInstance().getServerId(), uniqueId);
    }

    @Override
    public void joinPlayer(String serverId, UUID uniqueId) {
        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            Pipeline pipe = jedis.pipelined();
            pipe.sadd("server:" + serverId + ":players", uniqueId.toString());
            pipe.sync();
        }

        sendPacket(new ServerUpdate(serverId, uniqueId, ServerUpdate.UpdateType.JOIN));
    }

    @Override
    public void leavePlayer(UUID uniqueId) {
        leavePlayer(CommonPlugin.getInstance().getServerId(), uniqueId);
    }

    @Override
    public void leavePlayer(String serverId, UUID uniqueId) {
        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            Pipeline pipe = jedis.pipelined();
            pipe.sadd("server:" + serverId + ":players", uniqueId.toString());
            pipe.sync();
        }

        sendPacket(new ServerUpdate(serverId, uniqueId, ServerUpdate.UpdateType.LEAVE));
    }

    @Override
    public Set<UUID> getOnlinePlayers(String serverId) {
        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            return jedis.smembers("server:" + serverId + ":players").stream().map(UUID::fromString)
                        .collect(Collectors.toSet());
        }
    }

    @Override
    public Map<String, ProxiedServer> retrieveServerByType(ServerType... serversType) {
        Map<String, ProxiedServer> map = new HashMap<>();

        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            String[] str = new String[serversType.length];

            for (int i = 0; i < serversType.length; i++) {
                str[i] = "server:type:" + ServerType.values()[i].name().toLowerCase();
            }

            for (String serverId : jedis.sunion(str)) {
                map.put(serverId, load(jedis, serverId));
            }
        }

        return map;
    }

    @Override
    public Map<String, ProxiedServer> retrieveServerById(String... serversId) {
        Map<String, ProxiedServer> map = new HashMap<>();

        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            for (String serverId : serversId) {
                map.put(serverId, load(jedis, serverId));
            }
        }

        return map;
    }

    public ProxiedServer load(Jedis jedis, String serverId) {
        ServerType serverType = ServerType.getByName(jedis.hget("server:" + serverId, "type"));
        int maxPlayers = Integer.parseInt(jedis.hget("server:" + serverId, "maxplayers"));
        boolean joinEnabled = Boolean.parseBoolean(jedis.hget("server:" + serverId, "joinenabled"));
        String serverAddress = jedis.hget("server:" + serverId, "address");
        int serverPort = Integer.parseInt(jedis.hget("server:" + serverId, "port"));
                /*String serverMap = jedis.hget("server:" + serverId, "map");
                int time = Integer.parseInt(jedis.hget("server:" + serverId, "time"));
                String state = jedis.hget("server:" + serverId + ":type", "state");*/
        long startTime = Long.parseLong(jedis.hget("server:" + serverId, "starttime"));
        Set<UUID> players = jedis.smembers("server:" + serverId + ":players").stream().map(UUID::fromString)
                                 .collect(Collectors.toSet());

        ProxiedServer server = new ProxiedServer(serverAddress, serverPort, serverId, serverType, players, maxPlayers,
                                                 joinEnabled);

        server.setPlayers(players);
        return server;
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
}
