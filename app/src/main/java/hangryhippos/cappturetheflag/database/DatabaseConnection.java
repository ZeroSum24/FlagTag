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
    private static final String URI_STRING = "mongodb+srv://melon:rCgSxW5DHrO5PcWn@cluster0-zy4zv.mongodb.net";
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
