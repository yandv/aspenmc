package br.com.aspenmc.backend.data.redis;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.backend.data.ConnectionData;
import br.com.aspenmc.entity.sender.member.MemberConnection;
import br.com.aspenmc.utils.json.JsonUtils;
import com.google.common.base.Charsets;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RedisConnectionService implements ConnectionData {

    private static final String BASE_PATH = "connection:";
    private static final int TIME_TO_EXPIRE = 60 * 15;

    @Override
    public CompletableFuture<MemberConnection> retrieveConnection(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
                boolean exists = jedis.ttl(BASE_PATH + playerName.toLowerCase()) >= 0;

                if (exists) {
                    return JsonUtils
                            .mapToObject(jedis.hgetAll(BASE_PATH + playerName.toLowerCase()), MemberConnection.class)
                            .cache();
                }
            }

            UUID uniqueId = CommonPlugin.getInstance().getUuidFetcher().getUniqueId(playerName);

            if (uniqueId == null) {
                return new MemberConnection(playerName, UUID.nameUUIDFromBytes(
                        ("OfflinePlayer:" + playerName).getBytes(Charsets.UTF_8)), false);
            }

            return new MemberConnection(playerName, uniqueId, true);
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public void cacheConnection(MemberConnection memberConnection) {
        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            jedis.hmset(BASE_PATH + memberConnection.getPlayerName().toLowerCase(),
                        JsonUtils.objectToMap(memberConnection));
            jedis.expire(BASE_PATH + memberConnection.getPlayerName().toLowerCase(), TIME_TO_EXPIRE);
            jedis.save();
        }
    }
}
