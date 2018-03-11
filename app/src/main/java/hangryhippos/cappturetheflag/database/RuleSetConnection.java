package hangryhippos.cappturetheflag.database;

import android.support.v4.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.mongodb.client.MongoCollection;

import org.bson.Document;

import java.util.ArrayList;

import hangryhippos.cappturetheflag.database.obj.RuleSet;
import hangryhippos.cappturetheflag.database.obj.Utils;
import hangryhippos.cappturetheflag.database.obj.Zone;

/**
 * Created by colin on 10/03/18.
 */

public class RuleSetConnection
{
    private DatabaseConnection connection;

    private static final String COLLECTION_NAME_RULES = "game_rules";

    public RuleSetConnection()
    {
        this.connection = new DatabaseConnection();
    }

    public RuleSet getRuleSet()
    {
        try
        {
            MongoCollection collection = connection.getMongoDatabase().getCollection(COLLECTION_NAME_RULES);
            Document d = (Document) collection.find().first();

            int scoreLimit = d.getInteger("scoreLimit");
            int maxPerTeam = d.getInteger("playersPerTeam");
            Zone blueZone = Utils.getZone(d, "blueZone");
            Zone redZone = Utils.getZone(d, "redZone");
            Zone jailZone = Utils.getZone(d, "jailZone");
            Zone neutralZone = Utils.getZone(d, "neutralZone");

            Document blueFlagDoc = (Document) d.get("blueFlag");
            ArrayList<Double> blueFlagArray = (ArrayList<Double>) blueFlagDoc.get("coordinates");
            LatLng blueFlagLoc = new LatLng(blueFlagArray.get(0), blueFlagArray.get(1));

            Document redFlagDoc = (Document) d.get("redFlag");
            ArrayList<Double> redFlagArray = (ArrayList<Double>) redFlagDoc.get("coordinates");
            LatLng redFlagLoc = new LatLng(redFlagArray.get(0), redFlagArray.get(1));

            if(blueZone == null || redZone == null || jailZone == null || neutralZone == null)
            {
                return null;
            }

            return new RuleSet(scoreLimit, maxPerTeam, blueZone, redZone, jailZone, neutralZone, blueFlagLoc, redFlagLoc);

        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            // Log.e(TAG, e.getMessage());
        }

        return null;
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
