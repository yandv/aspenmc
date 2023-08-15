package br.com.aspenmc.backend.type;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import br.com.aspenmc.backend.Credentials;
import br.com.aspenmc.backend.Database;
import org.bson.Document;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Getter
public class MongoConnection implements Database {

    private static final String PATTERN = "([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])";

    private static final Pattern IP_PATTERN = Pattern.compile(
            PATTERN + "\\." + PATTERN + "\\." + PATTERN + "\\." + PATTERN);

    @Getter
    private com.mongodb.MongoClient client;
    @Getter
    private MongoDatabase db;

    private String url;

    private String dataBase;
    private int port;

    public MongoConnection(String url) {
        this.url = url;
    }

    public MongoConnection(String hostName, String userName, String passWord, String dataBase, int port) {
        this(IP_PATTERN.matcher(hostName).matches() ?
                "mongodb://" + (userName.isEmpty() ? "" : userName + ":" + passWord + "@") + hostName + "/" + dataBase +
                        "?retryWrites=true&w=majority" :
                "mongodb+srv://" + (userName.isEmpty() ? "" : userName + ":" + passWord + "@") + hostName + "/" +
                        dataBase + "?retryWrites=true&w=majority");
    }

    public MongoConnection(Credentials credentials) {
        this(credentials.getHostName(), credentials.getUserName(), credentials.getPassWord(), credentials.getDatabase(),
                credentials.getPort());
    }

    public MongoConnection(String hostName, String userName, String passWord, String dataBase) {
        this(hostName, userName, passWord, dataBase, 27017);
    }

    @Override
    public void createConnection() {
        MongoClientURI uri = new MongoClientURI(getUrl());
        this.dataBase = uri.getDatabase();

        client = new com.mongodb.MongoClient(new MongoClientURI(getUrl()));

        Logger.getLogger("uri").setLevel(Level.SEVERE);

        db = client.getDatabase(dataBase);
    }

    public MongoDatabase getDefaultDatabase() {
        return client.getDatabase(getDataBase());
    }

    public MongoDatabase getDatabase(String database) {
        return client.getDatabase(database);
    }

    public MongoCollection<Document> createCollection(String databaseName, String collectionName,
            Consumer<MongoCollection<Document>> consumer) {
        MongoDatabase mongoDatabase = getDatabase(databaseName);

        if (mongoDatabase.listCollectionNames().into(new ArrayList<>()).stream()
                         .noneMatch(name -> name.equalsIgnoreCase(collectionName))) {
            mongoDatabase.createCollection(collectionName);
            consumer.accept(mongoDatabase.getCollection(collectionName));
        }

        return mongoDatabase.getCollection(collectionName);
    }

    public MongoCollection<Document> createCollection(String collectionName,
            Consumer<MongoCollection<Document>> consumer) {
        return createCollection(getDataBase(), collectionName, consumer);
    }

    public MongoCollection<Document> createCollection(String collectionName) {
        return createCollection(collectionName, consumer -> {});
    }


    @Override
    public boolean isConnected() {
        return client != null;
    }

    @Override
    public void closeConnection() {
        client.close();
    }
}
