package br.com.aspenmc.backend.data.mongo;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.backend.data.StatusData;
import br.com.aspenmc.backend.type.MongoConnection;
import br.com.aspenmc.entity.member.status.Status;
import br.com.aspenmc.entity.member.status.StatusType;
import br.com.aspenmc.utils.json.JsonUtils;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MongoStatusData implements StatusData {

    private final MongoCollection<Document> statusCollection;

    public MongoStatusData(MongoConnection mongoConnection) {
        this.statusCollection = mongoConnection.createCollection("status");
    }


    @Override
    public CompletableFuture<Collection<Status>> getStatusById(UUID uniqueId) {
        return CompletableFuture.supplyAsync(() -> {
            return statusCollection.find(Filters.eq("uniqueId", uniqueId.toString())).into(new ArrayList<>()).stream()
                                   .map(document -> CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document),
                                           Status.class)).collect(Collectors.toList());
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public CompletableFuture<List<Status>> getStatusById(UUID uniqueId, Set<StatusType> preloadedStatus) {
        return CompletableFuture.supplyAsync(() -> {
            List<Status> status = new ArrayList<>();

            for (StatusType statusType : preloadedStatus) {
                Document document = statusCollection.find(Filters.and(Filters.eq("uniqueId", uniqueId.toString()),
                        Filters.eq("statusType", statusType.name()))).first();

                if (document == null) {
                    status.add(new Status(uniqueId, statusType));
                    continue;
                }

                status.add(CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document), Status.class));
            }

            return status;
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public CompletableFuture<Status> getStatusById(UUID uniqueId, StatusType statusType) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = statusCollection.find(Filters.and(Filters.eq("uniqueId", uniqueId.toString()),
                    Filters.eq("statusType", statusType.name()))).first();

            if (document == null) {
                Status status = new Status(uniqueId, statusType);

                CompletableFuture.runAsync(() -> {
                    statusCollection.insertOne(Document.parse(CommonConst.GSON.toJson(status)));
                }, CommonConst.PRINCIPAL_EXECUTOR);

                return status;
            }

            return CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document), Status.class);
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public CompletableFuture<List<Status>> ranking(StatusType statusType, String fieldName, int page, int perPage) {
        return CompletableFuture.supplyAsync(() -> {
            return statusCollection.find(Filters.eq("statusType", statusType.name())).sort(new Document(fieldName, -1))
                                   .skip((page - 1) * perPage).limit(perPage).into(new ArrayList<>()).stream()
                                   .map(document -> CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document),
                                           Status.class)).collect(Collectors.toList());
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }

    @Override
    public void saveStatus(Status status, String id) {
        CompletableFuture.runAsync(() -> {
            statusCollection.updateOne(Filters.and(Filters.eq("uniqueId", status.getUniqueId().toString()),
                    Filters.eq("statusType", status.getStatusType().name())), new Document("$set",
                    new Document("statusMap." + id,
                            JsonUtils.elementToBson(JsonUtils.jsonTree(status).get("statusMap." + id)))));
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }
}
