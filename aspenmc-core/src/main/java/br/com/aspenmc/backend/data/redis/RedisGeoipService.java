package br.com.aspenmc.backend.data.redis;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.backend.data.GeoipService;
import br.com.aspenmc.entity.ip.IpInfo;
import br.com.aspenmc.utils.json.JsonUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.concurrent.CompletableFuture;

public class RedisGeoipService implements GeoipService {

    @Override
    public CompletableFuture<IpInfo> getIp(String ip) {
        return CompletableFuture.supplyAsync(() -> {
            IpInfo ipInfo;

            try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
                Pipeline pipeline = jedis.pipelined();

                if (jedis.exists("ip:" + ip)) {
                    ipInfo = JsonUtils.mapToObject(jedis.hgetAll("ip:" + ip), IpInfo.class);
                    pipeline.expire("ip:" + ip, 60 * 60 * 24 * 7);
                } else {
                    ipInfo = GeoipService.retrieveIp(ip);

                    pipeline.hmset("ip:" + ip, JsonUtils.objectToMap(ipInfo));
                    pipeline.expire("ip:" + ip, 60 * 60 * 24 * 7);
                }

                pipeline.sync();
            }

            return ipInfo;
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }
}
