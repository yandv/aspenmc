package br.com.aspenmc.backend.data.mongo;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.backend.data.MemberService;
import br.com.aspenmc.backend.type.MongoConnection;
import br.com.aspenmc.entity.sender.member.Member;
import br.com.aspenmc.packet.type.member.MemberFieldUpdate;
import br.com.aspenmc.utils.json.JsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MongoMemberService implements MemberService {

    private final MongoCollection<Document> memberCollection;

    public MongoMemberService(MongoConnection mongoConnection) {
        this.memberCollection = mongoConnection.createCollection("members", collection -> {
            collection.createIndex(new Document("uniqueId", 1), new IndexOptions().unique(true));
            collection.createIndex(new Document("name", 1), new IndexOptions().unique(true));
        });
    }

    @Override
    public <T extends Member> CompletableFuture<T> getMemberById(UUID playerId, Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            T t = getRedisPlayer(playerId, clazz);

            if (t != null) {
                return t;
            }

            Document document = memberCollection.find(Filters.eq("uniqueId", playerId.toString())).first();
            return document == null ? null : CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document), clazz);
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public <T extends Member> CompletableFuture<T> getMemberByName(String playerName, Class<T> clazz,
            boolean ignoreCase) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = memberCollection.find(Filters.eq("name",
                                                        ignoreCase ?
                                                                new Document("$regex", "^" + playerName + "$").append("$options", "i") : playerName))
                                                .first();
            return document == null ? null : CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document), clazz);
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public <T extends Member> CompletableFuture<List<T>> getMembers(Bson bson, Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            return memberCollection.find(bson).into(new ArrayList<>()).stream()
                                   .map(document -> CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document), clazz))
                                   .collect(Collectors.toList());
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    public <T extends Member> T getRedisPlayer(UUID uuid, Class<T> clazz) {
        Member player;

        try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
            if (!jedis.exists("account:" + uuid.toString())) {
                return null;
            }

            Map<String, String> fields = jedis.hgetAll("account:" + uuid.toString());

            if (fields == null || fields.isEmpty() || fields.size() < Member.class.getDeclaredFields().length - 1) {
                return null;
            }

            player = JsonUtils.mapToObject(fields, clazz);
        }

        return clazz.cast(player);
    }

    @Override
    public void updateMany(String fieldName, Object value, UUID... ids) {
        CompletableFuture.runAsync(() -> {
            String[] fields = new String[] { fieldName };
            JsonElement[] values = new JsonElement[] { value == null ? JsonNull.INSTANCE : JsonUtils.jsonTree(value) };

            for (UUID id : ids) {
                if (value != null) {
                    memberCollection.updateOne(Filters.eq("uniqueId", id.toString()),
                            new Document("$set", new Document(fieldName, JsonUtils.elementToBson(values[0]))));
                } else {
                    memberCollection.updateOne(Filters.eq("uniqueId", id.toString()),
                            new Document("$unset", new Document(fieldName, "")));
                }
                CommonPlugin.getInstance().getPacketManager().sendPacket(new MemberFieldUpdate(id, fields, values));
            }
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public void updateMember(Member member, String... fields) {
        CompletableFuture.runAsync(() -> {
            JsonObject tree = JsonUtils.jsonTree(member);

            List<Bson> updatePredicates = new ArrayList<>();
            JsonElement[] values = new JsonElement[fields.length];

            for (int i = 0; i < fields.length; i++) {
                String fieldName = fields[i];

                if (fieldName.contains(".")) {
                    String field = fieldName.split("\\.")[0];
                    String subField = fieldName.split("\\.")[1];

                    if (tree.has(field) && tree.get(field).isJsonObject()) {
                        JsonObject jsonObject = tree.getAsJsonObject(field);

                        if (jsonObject.has(subField)) {
                            updatePredicates.add(
                                    Updates.set(fieldName, JsonUtils.elementToBson(jsonObject.get(subField))));
                            values[i] = jsonObject.get(subField);
                            continue;
                        }
                    }
                } else if (tree.has(fieldName)) {
                    updatePredicates.add(Updates.set(fieldName, JsonUtils.elementToBson(tree.get(fieldName))));
                    values[i] = tree.get(fieldName);
                    continue;
                }

                updatePredicates.add(Updates.unset(fieldName));
                values[i] = JsonNull.INSTANCE;
            }

            memberCollection.updateOne(Filters.eq("uniqueId", member.getUniqueId().toString()),
                    Updates.combine(updatePredicates));
            CommonPlugin.getInstance().getPacketManager()
                        .sendPacket(new MemberFieldUpdate(member.getUniqueId(), fields, values));
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public void createMember(Member member) {
        memberCollection.insertOne(Document.parse(CommonConst.GSON.toJson(member)));
    }

    @Override
    public void deleteMember(UUID uniqueId) {
        memberCollection.deleteOne(Filters.eq("uniqueId", uniqueId.toString()));
    }
}
