package br.com.aspenmc.backend.data.mongo;

import br.com.aspenmc.backend.type.MongoConnection;
import br.com.aspenmc.packet.type.server.group.GroupCreate;
import br.com.aspenmc.packet.type.server.group.GroupDelete;
import br.com.aspenmc.packet.type.server.group.GroupFieldUpdate;
import br.com.aspenmc.packet.type.server.tag.TagCreate;
import br.com.aspenmc.packet.type.server.tag.TagDelete;
import br.com.aspenmc.packet.type.server.tag.TagFieldUpdate;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.backend.data.PermissionData;
import br.com.aspenmc.permission.Group;
import br.com.aspenmc.permission.Tag;
import br.com.aspenmc.utils.json.JsonUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MongoPermissionData implements PermissionData {

    private final MongoCollection<Document> groupCollection;
    private final MongoCollection<Document> tagCollection;

    public MongoPermissionData(MongoConnection mongoConnection) {
        this.groupCollection = mongoConnection.createCollection("groups", collection -> {
            collection.createIndex(new Document("id", 1), new IndexOptions().unique(true));
            collection.createIndex(new Document("groupName", 1), new IndexOptions().unique(true));
            collection.insertOne(Document.parse(
                    CommonConst.GSON.toJson(new Group(0, "Membro", Arrays.asList("tag.membro"), "membro", true, false))));
        });

        this.tagCollection = mongoConnection.createCollection("tags", collection -> {
            collection.createIndex(new Document("id", 1), new IndexOptions().unique(true));
            collection.createIndex(new Document("tagName", 1), new IndexOptions().unique(true));
            collection.insertOne(Document.parse(CommonConst.GSON.toJson(new Tag(0, "Membro", "§f"))));
        });
    }

    @Override
    public void createGroup(Group group) {
        groupCollection.insertOne(Document.parse(CommonConst.GSON.toJson(group)));
        CommonPlugin.getInstance().getPacketManager().sendPacketAsync(new GroupCreate(group));
    }

    @Override
    public void createTag(Tag tag) {
        tagCollection.insertOne(Document.parse(CommonConst.GSON.toJson(tag)));
        CommonPlugin.getInstance().getPacketManager().sendPacketAsync(new TagCreate(tag));
    }

    @Override
    public Optional<Group> retrieveGroupByName(String groupName) {
        Document document = groupCollection.find(new Document("groupName", groupName)).first();
        return document == null ? Optional.empty() :
               Optional.of(CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document), Group.class));
    }

    @Override
    public Optional<Tag> retrieveTagByName(String tagName) {
        Document document = tagCollection.find(new Document("tagName", tagName)).first();
        return document == null ? Optional.empty() :
               Optional.of(CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document), Tag.class));
    }

    @Override
    public Optional<Group> retrieveGroupById(int groupId) {
        Document document = groupCollection.find(new Document("id", groupId)).first();
        return document == null ? Optional.empty() :
               Optional.of(CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document), Group.class));
    }

    @Override
    public Optional<Tag> retrieveTagById(int tagId) {
        Document document = tagCollection.find(new Document("id", tagId)).first();
        return document == null ? Optional.empty() :
               Optional.of(CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document), Tag.class));
    }

    @Override
    public List<Group> retrieveAllGroups() {
        return groupCollection.find().into(new ArrayList<>()).stream()
                              .map(document -> CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document),
                                                                         Group.class)).collect(Collectors.toList());
    }

    @Override
    public List<Tag> retrieveAllTags() {
        return tagCollection.find().into(new ArrayList<>()).stream()
                            .map(document -> CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document), Tag.class))
                            .collect(Collectors.toList());
    }

    @Override
    public void deleteGroup(Group group) {
        groupCollection.deleteOne(new Document("id", group.getId()));
        CommonPlugin.getInstance().getPacketManager().sendPacketAsync(new GroupDelete(group.getGroupName()));
    }

    @Override
    public void deleteTag(Tag tag) {
        tagCollection.deleteOne(new Document("id", tag.getId()));
        CommonPlugin.getInstance().getPacketManager().sendPacketAsync(new TagDelete(tag.getTagName()));
    }

    @Override
    public void updateGroup(Group group, String... fields) {
        JsonObject tree = JsonUtils.jsonTree(group);
        JsonElement[] values = new JsonElement[fields.length];

        for (int i = 0; i < fields.length; i++) {
            String fieldName = fields[i];

            if (tree.has(fieldName)) {
                groupCollection.updateOne(Filters.eq("id", group.getId()), new Document("$set", new Document(fieldName,
                                                                                                             JsonUtils.elementToBson(
                                                                                                                     tree.get(
                                                                                                                             fieldName)))));
                values[i] = tree.get(fieldName);
            } else {
                groupCollection.updateOne(Filters.eq("id", group.getId()),
                                          new Document("$unset", new Document(fieldName, "")));
                values[i] = JsonNull.INSTANCE;
            }
        }

        CommonPlugin.getInstance().getPacketManager()
                    .sendPacketAsync(new GroupFieldUpdate(group.getGroupName(), fields, values));
    }

    @Override
    public void updateTag(Tag tag, String... fields) {
        JsonObject tree = JsonUtils.jsonTree(tag);
        JsonElement[] values = new JsonElement[fields.length];

        for (int i = 0; i < fields.length; i++) {
            String fieldName = fields[i];

            if (tree.has(fieldName)) {
                tagCollection.updateOne(Filters.eq("id", tag.getId()), new Document("$set", new Document(fieldName,
                                                                                                         JsonUtils.elementToBson(
                                                                                                                 tree.get(
                                                                                                                         fieldName)))));
                values[i] = tree.get(fieldName);
            } else {
                tagCollection.updateOne(Filters.eq("id", tag.getId()),
                                        new Document("$unset", new Document(fieldName, "")));
                values[i] = JsonNull.INSTANCE;
            }
        }

        CommonPlugin.getInstance().getPacketManager()
                    .sendPacketAsync(new TagFieldUpdate(tag.getTagName(), fields, values));
    }
}
