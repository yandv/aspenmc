package br.com.aspenmc.backend.type;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.backend.Credentials;
import br.com.aspenmc.backend.Database;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.logging.Level;

@RequiredArgsConstructor
public class RedisConnection implements Database {

    @NonNull
    private final String hostname, password;
    private final int port;

    @Getter
    private JedisPool pool;

    public RedisConnection() {
        this("localhost", "", 6379);
    }

    public RedisConnection(Credentials credentials) {
        this(credentials.getHostName(), credentials.getPassWord(), credentials.getPort());
    }

    @Override
    public void createConnection() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(128);
        if (!password.isEmpty()) {
            pool = new JedisPool(config, hostname, port, 0, password);
        } else {
            pool = new JedisPool(config, hostname, port, 0);
        }
    }

    @Override
    public boolean isConnected() {
        return !pool.isClosed();
    }

    @Override
    public void closeConnection() {
        if (pool != null) {
            pool.destroy();
        }
    }

    public static class PubSubListener implements Runnable {

        private RedisConnection redis;
        private JedisPubSub jpsh;

        private final String[] channels;

        public PubSubListener(RedisConnection redis, JedisPubSub s, String... channels) {
            this.redis = redis;
            this.jpsh = s;
            this.channels = channels;
        }

        @Override
        public void run() {
            CommonPlugin.getInstance().getLogger().log(Level.INFO, "Loading jedis!");

            try (Jedis jedis = redis.getPool().getResource()) {
                try {
                    jedis.subscribe(jpsh, channels);
                } catch (Exception e) {
                    CommonPlugin.getInstance().getLogger().log(Level.INFO, "PubSub error, attempting to recover.", e);
                    try {
                        jpsh.unsubscribe();
                    } catch (Exception e1) {

                    }
                    run();
                }
            }
        }

        public void addChannel(String... channel) {
            jpsh.subscribe(channel);
        }

        public void removeChannel(String... channel) {
            jpsh.unsubscribe(channel);
        }

        public void poison() {
            jpsh.unsubscribe();
        }
    }
}
