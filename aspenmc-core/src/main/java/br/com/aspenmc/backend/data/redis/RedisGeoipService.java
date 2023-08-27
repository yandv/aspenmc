package br.com.aspenmc.backend.data.redis;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.backend.data.GeoipService;
import br.com.aspenmc.utils.geoip.IpInfo;
import br.com.aspenmc.utils.json.JsonUtils;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RedisGeoipService implements GeoipService {

    @Override
    public CompletableFuture<IpInfo> getIp(String ip) {
        return CompletableFuture.supplyAsync(() -> {
            IpInfo ipInfo;

            try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
                if (!jedis.exists("ip:" + ip)) {
                    ipInfo = IpInfo.retrieveIp(ip);

                    jedis.hmset("ip:" + ip, JsonUtils.objectToMap(ipInfo));
                    jedis.expire("ip:" + ip, 60 * 60 * 24 * 7);

                    return ipInfo;
                }

                ipInfo = JsonUtils.mapToObject(jedis.hgetAll("ip:" + ip), IpInfo.class);
                jedis.expire("ip:" + ip, 60 * 60 * 24 * 7);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return ipInfo;
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }
}
