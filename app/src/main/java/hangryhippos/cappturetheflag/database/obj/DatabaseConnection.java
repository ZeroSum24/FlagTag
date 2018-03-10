package hangryhippos.cappturetheflag.database.obj;

import android.annotation.SuppressLint;
import android.support.v4.util.Pair;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import java.util.ArrayList;

/**
 * Connects to database and retrieves data
 */

public class DatabaseConnection
{
    private static final String TAG = "DatabaseConnection";

    @SuppressLint("AuthLeak")
    private static final String URI_STRING = "mongodb+srv://melon:rCgSxW5DHrO5PcWn@cluster0-zy4zv.mongodb.net";
    private static final String DB_NAME = "ctf";
    private static final String COLLECTION_NAME_RULES = "game_rules";

    private MongoClientURI uri;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    private static final String TOP_LEFT = "topLeft";
    private static final String BOTTOM_RIGHT = "bottomRight";


    public DatabaseConnection()
    {
        uri = new MongoClientURI(URI_STRING);
        mongoClient = new MongoClient(uri);
        mongoDatabase = mongoClient.getDatabase(DB_NAME);
            }

    public RuleSet getRuleSet()
    {
        try
        {
            MongoCollection collection = mongoDatabase.getCollection(COLLECTION_NAME_RULES);
            Document d = (Document) collection.find().first();

            int scoreLimit = d.getInteger("scoreLimit");
            int maxPerTeam = d.getInteger("playersPerTeam");
            Zone blueZone = getZone(d, "blueZone");
            Zone redZone = getZone(d, "redZone");
            Zone jailZone = getZone(d, "jailZone");
            Zone neutralZone = getZone(d, "neutralZone");

            if(blueZone == null || redZone == null || jailZone == null || neutralZone == null)
            {
                return null;
            }

            return new RuleSet(scoreLimit, maxPerTeam, blueZone, redZone, jailZone, neutralZone);

        }
        catch(Exception e)
        {
           // Log.e(TAG, e.getMessage());
            System.out.println(e.getMessage());
        }

        return null;
    }

    private Zone getZone(Document doc, String parentKey)
    {

        try
        {
            Document top = (Document) doc.get(parentKey);
            Document topLeft = (Document) top.get(TOP_LEFT);
            Document bottomRight = (Document) top.get(BOTTOM_RIGHT);

            ArrayList<Double> topLeftArray = (ArrayList<Double>) topLeft.get("coordinates");
            ArrayList<Double> bottomRightArray = (ArrayList<Double>) bottomRight.get("coordinates");

            Pair<Double, Double> topLeftPair = new Pair<>(topLeftArray.get(0), topLeftArray.get(1));
            Pair<Double, Double> bottomRightPair = new Pair<>(bottomRightArray.get(0), bottomRightArray.get(1));

            return new Zone(topLeftPair, bottomRightPair);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }

    }
}
