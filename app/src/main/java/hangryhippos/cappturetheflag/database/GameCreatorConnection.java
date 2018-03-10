package hangryhippos.cappturetheflag.database;

import com.google.android.gms.maps.model.LatLng;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import org.bson.Document;

import java.util.ArrayList;

import hangryhippos.cappturetheflag.database.obj.Utils;

import static com.mongodb.client.model.Filters.eq;

/**
 * Used to set initial state of game session in database
 */

public class GameCreatorConnection {
    private static final String GAME_SESSION_COLLECTION_NAME = "sessions";

    private DatabaseConnection connection;
    private int sessionID = 0;

    private String displayName;
    private String deviceID;
    private LatLng startLocation;

    public GameCreatorConnection(LatLng startLocation, String deviceID, String displayName) {
        this.deviceID = deviceID;
        this.connection = new DatabaseConnection();
        this.displayName = displayName;
        this.startLocation = startLocation;
    }

    public static boolean isGameInProgress()
    {
        MongoCollection collection = new DatabaseConnection().getMongoDatabase().getCollection(GAME_SESSION_COLLECTION_NAME);
        Document d = (Document) collection.find().first();

        if(d == null)
            return false;

        return d.getBoolean("in_progress", false);
    }

    public void registerNewGame() {
        MongoCollection collection = connection.getMongoDatabase().getCollection(GAME_SESSION_COLLECTION_NAME);

        Document document = new Document();
        document.put("_id", sessionID);
        document.put("in_progress", true);
        document.put("host", deviceID);
        document.put("blueTeam", buildTeamDoc(true));
        document.put("redTeam", buildTeamDoc(false));
        System.out.println(document.toJson());

        collection.replaceOne(eq("_id", 0), document, new UpdateOptions().upsert(true));

    }

    private Document buildTeamDoc(boolean inTeam) {
        Document membersDoc = new Document();
        ArrayList<Document> members = new ArrayList<>();
        Document memberDoc = new Document();

        if (inTeam) {
            memberDoc.put("deviceID", deviceID);
            memberDoc.put("displayName", displayName);
            memberDoc.put("isJailed", false);
            memberDoc.put("numOfCaps", 0);
            memberDoc.put("numOfTags", 0);
            memberDoc.put("numOfJails", 0);

            Document location = (Document) Utils.buildLocationDoc(startLocation.latitude, startLocation.longitude).get("location");
            memberDoc.put("location", location);
            memberDoc.put("items", new ArrayList<>());
            members.add(memberDoc);
        }

        membersDoc.put("members", members);
        return membersDoc;
    }

    private Document buildFlagDoc(double latitude, double longitude, String carrier) {
        Document location = (Document) Utils.buildLocationDoc(latitude, longitude).get("location");

        Document parent = new Document();
        parent.put("carrier", carrier);
        parent.put("location", location);

        return parent;
    }


}