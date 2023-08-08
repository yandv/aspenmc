package br.com.aspenmc.backend.data.redis;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.backend.data.SkinData;
import br.com.aspenmc.entity.member.Skin;
import br.com.aspenmc.utils.json.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RedisSkinData implements SkinData {

    private static final String BASE_PATH = "skin-data:";
    private static final int TIME_TO_EXPIRE = 60 * 60 * 12;


    @Override
    public Optional<Skin> loadData(String playerName) {
        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            boolean exists = jedis.ttl(BASE_PATH + playerName.toLowerCase()) >= 0;

            if (exists) {
                Map<String, String> fields = jedis.hgetAll(BASE_PATH + playerName.toLowerCase());

                if (fields != null && !fields.isEmpty()) {
                    return Optional.of(JsonUtils.mapToObject(fields, Skin.class));
                }
            }
        }

        UUID uniqueId = CommonPlugin.getInstance().getMojangId(playerName);

        if (uniqueId == null) {
            return Optional.empty();
        }

        String[] skin = loadSkinById(uniqueId);

        if (skin == null) {
            return Optional.empty();
        }

        Skin skinData = new Skin(playerName, uniqueId, skin[0], skin[1]);

        save(skinData, TIME_TO_EXPIRE);

        return Optional.of(skinData);
    }

    @Override
    public CompletableFuture<Skin> loadUserData(String playerName) {
        return CompletableFuture.supplyAsync(() -> loadData(playerName).orElse(null), CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public void save(Skin skin, int seconds) {
        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            jedis.hmset(BASE_PATH + skin.getPlayerName().toLowerCase(), JsonUtils.objectToMap(skin));
            jedis.expire(BASE_PATH + skin.getPlayerName().toLowerCase(), 60 * 60 * 24 * 3);
            jedis.save();
        }
    }

    @Override
    public String[] loadSkinById(UUID uuid) {
        try {
            URLConnection con = new URL(String.format(CommonConst.SKIN_FETCHER, uuid.toString())).openConnection();
            JsonElement element = JsonParser.parseReader(
                    new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)));

            if (element instanceof JsonObject) {
                JsonObject object = element.getAsJsonObject();

                if (object.has("properties")) {
                    JsonArray jsonArray = object.get("properties").getAsJsonArray();
                    JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();

                    String value = jsonObject.get("value").getAsString();
                    String signature = jsonObject.has("signature") ? jsonObject.get("signature").getAsString() : "";
                    return new String[] { value, signature };
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public CompletableFuture<Skin> loadSkinById(UUID uuid, String skinName) {
        return CompletableFuture.supplyAsync(() -> {
            String[] skinData = loadSkinById(uuid);
            return new Skin(skinName, uuid, skinData[0], skinData[1]);
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }
}
