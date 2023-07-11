package br.com.aspenmc.backend.data.mongo;

import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.backend.data.GamerData;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.member.gamer.Gamer;
import br.com.aspenmc.utils.json.JsonUtils;
import org.bson.Document;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MongoGamerData implements GamerData {

    @Override
    public final <T extends Gamer> CompletableFuture<T> loadGamer(UUID uniqueId, String gamerId, Class<T> gamerClass) {
        return CompletableFuture.supplyAsync(() -> {
            Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(uniqueId).orElse(null);

            if (member != null && member.getGamer(gamerId) != null) {
                return gamerClass.cast(member.getGamer(gamerId));
            }

            Document document = getCollection(gamerId).find(Filters.eq("uniqueId", uniqueId.toString())).first();

            if (document == null) {
                Gamer gamer = null;

                try {
                    Constructor<T> constructor = gamerClass.getConstructor(UUID.class);
                    constructor.setAccessible(true);
                    gamer = constructor.newInstance(uniqueId);
                    return gamerClass.cast(gamer);
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                         InvocationTargetException e) {
                    throw new RuntimeException(e);
                } finally {
                    getCollection(gamerId).insertOne(Document.parse(CommonConst.GSON.toJson(gamer)));
                }
            } else {
                return CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document), gamerClass);
            }
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final CompletableFuture<List<? extends Gamer>> loadGamer(UUID uniqueId, Map.Entry<String, Class<? extends Gamer>>... classes) {
        Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(uniqueId).orElse(null);
        return CompletableFuture.supplyAsync(() -> Stream.of(classes).parallel().map(entry -> {
            if (member != null && member.getGamer(entry.getKey()) != null) {
                return entry.getValue().cast(member.getGamer(entry.getKey()));
            }

            Document document = getCollection(entry.getKey()).find(Filters.eq("uniqueId", uniqueId.toString())).first();

            if (document == null) {
                Gamer gamer = null;

                try {
                    Constructor<? extends Gamer> constructor = entry.getValue().getConstructor(UUID.class);
                    constructor.setAccessible(true);
                    gamer = constructor.newInstance(uniqueId);
                    return gamer;
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                         InvocationTargetException e) {
                    throw new RuntimeException(e);
                } finally {
                    getCollection(entry.getKey()).insertOne(Document.parse(CommonConst.GSON.toJson(gamer)));
                }
            } else {
                return CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document), entry.getValue());
            }
        }).collect(Collectors.toList()), CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public void save(Gamer gamer, String[] fields) {
        CompletableFuture.runAsync(() -> {
            JsonObject tree = JsonUtils.jsonTree(gamer);

            for (String fieldName : fields) {
                if (tree.has(fieldName)) {
                    getCollection(gamer.getId()).updateOne(Filters.eq("uniqueId", gamer.getUniqueId().toString()),
                                                           new Document("$set", new Document(fieldName,
                                                                                             JsonUtils.elementToBson(
                                                                                                     tree.get(
                                                                                                             fieldName)))));
                } else {
                    getCollection(gamer.getId()).updateOne(Filters.eq("uniqueId", gamer.getUniqueId().toString()),
                                                           new Document("$unset", new Document(fieldName, "")));
                }
            }
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    public MongoCollection<Document> getCollection(String gamerId) {
        return CommonPlugin.getInstance().getMongoConnection().getDefaultDatabase()
                           .getCollection("gamer_" + gamerId.toLowerCase());
    }
}
