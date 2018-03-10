package hangryhippos.cappturetheflag.database;

import android.support.v4.util.Pair;

import com.mongodb.client.MongoCollection;

import org.bson.Document;

import java.util.ArrayList;

import hangryhippos.cappturetheflag.database.obj.RuleSet;
import hangryhippos.cappturetheflag.database.obj.Zone;

/**
 * Created by colin on 10/03/18.
 */

public class RuleSetConnection
{
    private DatabaseConnection connection;

    private static final String COLLECTION_NAME_RULES = "game_rules";
    private static final String TOP_LEFT = "topLeft";
    private static final String BOTTOM_RIGHT = "bottomRight";


    public RuleSetConnection(DatabaseConnection connection)
    {
        this.connection = connection;
    }

    public RuleSet getRuleSet()
    {
        try
        {
            MongoCollection collection = connection.getMongoDatabase().getCollection(COLLECTION_NAME_RULES);
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

    public DatabaseConnection getConnection()
    {
        return connection;
    }

    public void setConnection(DatabaseConnection connection)
    {
        this.connection = connection;
    }
}
