package br.com.aspenmc.backend.data.mongo;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.backend.Credentials;
import br.com.aspenmc.backend.data.StatusService;
import br.com.aspenmc.backend.type.MongoConnection;
import br.com.aspenmc.entity.member.status.Status;
import br.com.aspenmc.entity.member.status.StatusField;
import br.com.aspenmc.entity.member.status.StatusType;
import br.com.aspenmc.utils.json.JsonUtils;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MongoStatusService implements StatusService {

    private final MongoCollection<Document> statusCollection;

    public MongoStatusService(MongoConnection mongoConnection) {
        this.statusCollection = mongoConnection.createCollection("status");
    }

    public static void main(String[] args) {
        MongoConnection mongoConnection = new MongoConnection(new Credentials("127.0.0.1", "", "", "aspenmc", 27017));
        mongoConnection.createConnection();

        UUID uniqueId = UUID.fromString("a0a0a0a0-a0a0-a0a0-a0a0-a0a0a0a0a0a0");
        MongoStatusService mongoStatusData = new MongoStatusService(mongoConnection);

        Status status = mongoStatusData.getStatusById(uniqueId, StatusType.FPS).join();

        status.set(StatusField.KILLSTREAK, 1);
        System.out.println(status.get(StatusField.KILLSTREAK));

        mongoStatusData.saveStatus(status, StatusField.KILLSTREAK.toField());
    }

    @Override
    public CompletableFuture<Collection<Status>> getStatusById(UUID uniqueId) {
        return CompletableFuture.supplyAsync(
                () -> statusCollection.find(Filters.eq("uniqueId", uniqueId.toString())).into(new ArrayList<>())
                                      .stream()
                                      .map(document -> CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document),
                                              Status.class)).collect(Collectors.toList()),
                CommonConst.PRINCIPAL_EXECUTOR);
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
    public void saveStatus(Status status, String... fields) {
        CompletableFuture.runAsync(() -> {
            JsonObject tree = JsonUtils.jsonTree(status);
            JsonObject jsonObject = tree.get("statusMap").getAsJsonObject();

            List<Bson> updatePredicates = new ArrayList<>();

            for (String fieldName : fields) {

                if (fieldName.contains("statusMap")) {
                    String field = fieldName.split("\\.")[1];

                    if (jsonObject.has(field)) {
                        updatePredicates.add(Updates.set(fieldName,
                                JsonUtils.elementToBson(jsonObject.get(field))));
                    } else {
                        updatePredicates.add(Updates.unset(fieldName));
                    }
                } else {
                    if (tree.has(fieldName)) {
                        updatePredicates.add(Updates.set(fieldName, JsonUtils.elementToBson(tree.get(fieldName))));
                    } else {
                        updatePredicates.add(Updates.unset(fieldName));
                    }
                }
            }

            statusCollection.updateOne(Filters.and(Filters.eq("uniqueId", status.getUniqueId().toString()),
                    Filters.eq("statusType", status.getStatusType().name())), Updates.combine(updatePredicates));
        }, CommonConst.PRINCIPAL_EXECUTOR);
    }
}
