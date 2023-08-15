package br.com.aspenmc.backend.data.mongo;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.backend.data.ClanData;
import br.com.aspenmc.backend.type.MongoConnection;
import br.com.aspenmc.clan.Clan;
import br.com.aspenmc.entity.Member;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MongoClanData implements ClanData {

    private final MongoCollection<Document> clanCollection;

    public MongoClanData(MongoConnection mongoConnection) {
        this.clanCollection = mongoConnection.createCollection("clan");
    }

    @Override
    public <T extends Clan> CompletableFuture<T> getClanById(UUID clanId, Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = clanCollection.find(Filters.eq("clanId", clanId.toString())).first();
            return document == null ? null : CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document), clazz);
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public <T extends Clan> CompletableFuture<T> getClanByName(String clanName, String clanAbbreviation,
            Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = clanCollection.find(Filters.or(
                    Filters.eq("clanName", new Document("$regex", "^" + clanName + "$").append("$options", "i")),
                    Filters.eq("clanAbbreviation",
                            new Document("$regex", "^" + clanAbbreviation + "$").append("$options", "i")))).first();
            return document == null ? null : CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document), clazz);
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public <T extends Clan> T createClan(T clan) {
        CompletableFuture.runAsync(() -> clanCollection.insertOne(Document.parse(CommonConst.GSON.toJson(clan))),
                CommonConst.PRINCIPAL_EXECUTOR);
        return clan;
    }

    @Override
    public CompletableFuture<UUID> getClanId() {
        return CompletableFuture.supplyAsync(() -> {
            UUID uuid = UUID.randomUUID();

            while (clanCollection.find(Filters.eq("clanId", uuid.toString())).first() != null) {
                uuid = UUID.randomUUID();
            }

            return uuid;
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public void deleteClan(Clan clan) {
        CompletableFuture.runAsync(() -> clanCollection.deleteOne(Filters.eq("clanId", clan.getClanId().toString())),
                CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public void updateClan(Clan clan, String... fields) {

    }
}
