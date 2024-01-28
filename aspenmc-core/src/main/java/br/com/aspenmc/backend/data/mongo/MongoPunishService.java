package br.com.aspenmc.backend.data.mongo;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.backend.data.PunishService;
import br.com.aspenmc.backend.type.MongoConnection;
import br.com.aspenmc.entity.sender.Sender;
import br.com.aspenmc.entity.sender.member.Member;
import br.com.aspenmc.punish.Punish;
import br.com.aspenmc.punish.PunishType;
import br.com.aspenmc.utils.json.JsonUtils;
import com.google.gson.JsonObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MongoPunishService implements PunishService {

    private final MongoCollection<Document> punishCollection;

    public MongoPunishService(MongoConnection mongoConnection) {
        this.punishCollection = mongoConnection.createCollection("punishs", collection -> {
            collection.createIndex(new Document("punishId", 1), new IndexOptions().unique(true));
        });
    }

    @Override
    public CompletableFuture<Punish> createPunish(Member target, Sender sender, PunishType punishType, String reason,
            long expiresAt) {
        return CompletableFuture.supplyAsync(() -> {
            String punishId = "";

            do {
                punishId = CommonConst.RANDOM.nextLong() + "";
            } while (punishCollection.find(Filters.eq("punishId", punishId)).first() != null);

            Punish punish = new Punish(punishId, target.getUniqueId(), sender.getUniqueId(), sender.getName(),
                    punishType, reason, expiresAt);
            punishCollection.insertOne(Document.parse(CommonConst.GSON.toJson(punish)));
            return punish;
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public CompletableFuture<Punish> getPunishById(String punishId) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = punishCollection.find(Filters.eq("punishId", punishId)).first();
            return document == null ? null : CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document), Punish.class);
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public CompletableFuture<Map<PunishType, List<Punish>>> getPunish(int page, int limit, String[] filters,
            Object... values) {
        return CompletableFuture.supplyAsync(() -> {
            Bson filter = null;

            for (int i = 0; i < filters.length; i++) {
                String field = filters[i];
                Object value = values[i];

                if (filter == null) {
                    filter = Filters.eq(field, value);
                } else {
                    filter = Filters.and(filter, Filters.eq(field, value));
                }
            }

            if (filter == null) {
                filter = new Document();
            }

            Map<PunishType, List<Punish>> punishMap = new HashMap<>();
            FindIterable<Document> iterable = punishCollection.find(filter).skip((page - 1) * limit).limit(limit);

            for (Document value : iterable) {
                Punish punish = CommonConst.GSON.fromJson(CommonConst.GSON.toJson(value), Punish.class);
                punishMap.computeIfAbsent(punish.getPunishType(), k -> new ArrayList<>()).add(punish);
            }

            return punishMap;
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public void updatePunish(Punish punish, String... fields) {
        CompletableFuture.runAsync(() -> {
            JsonObject tree = JsonUtils.jsonTree(punish);

            for (String fieldName : fields) {
                if (tree.has(fieldName)) {
                    punishCollection.updateOne(Filters.eq("punishId", punish.getPunishId()), new Document("$set",
                            new Document(fieldName, JsonUtils.elementToBson(tree.get(fieldName)))));
                } else {
                    punishCollection.updateOne(Filters.eq("punishId", punish.getPunishId()),
                            new Document("$unset", new Document(fieldName, "")));
                }
            }
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }
}
