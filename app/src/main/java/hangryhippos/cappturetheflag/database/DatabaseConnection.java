package hangryhippos.cappturetheflag.database;

import android.annotation.SuppressLint;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

/**
 * Connects to database and retrieves data
 */

public class DatabaseConnection
{
    private static final String TAG = "DatabaseConnection";

    @SuppressLint("AuthLeak")
    private static final String URI_STRING = "mongodb://melon:rCgSxW5DHrO5PcWn@cluster0-shard-00-00-zy4zv.mongodb.net:27017,cluster0-shard-00-01-zy4zv.mongodb.net:27017,cluster0-shard-00-02-zy4zv.mongodb.net:27017/test?ssl=true&replicaSet=Cluster0-shard-0&authSource=admin";
    // private static final String URI_STRING = "mongodb+srv://melon:rCgSxW5DHrO5PcWn@cluster0-zy4zv.mongodb.net";
    private static final String DB_NAME = "ctf";

    private MongoClientURI uri;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;


    public DatabaseConnection()
    {
        uri = new MongoClientURI(URI_STRING);
        mongoClient = new MongoClient(uri);
        mongoDatabase = mongoClient.getDatabase(DB_NAME);
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }
}
