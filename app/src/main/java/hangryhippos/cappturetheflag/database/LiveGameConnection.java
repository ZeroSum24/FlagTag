package hangryhippos.cappturetheflag.database;

import com.google.android.gms.maps.model.LatLng;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import java.util.ArrayList;

import hangryhippos.cappturetheflag.database.obj.Team;
import hangryhippos.cappturetheflag.database.obj.Utils;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.addToSet;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.pull;

/**
 * Used by a player when they are in a live game to update their state, and
 * other objects state such as flags, powerups when they interact with them
 */

public class LiveGameConnection {
    private static final String GAME_SESSION_COLLECTION_NAME = "sessions";

    private LatLng location;
    private String deviceID;
    private String displayName;
    private MongoCollection collection;

    public LiveGameConnection(LatLng location, String deviceID, String dislayName) {
        this.location = location;
        this.deviceID = deviceID;
        this.displayName = dislayName;
        this.collection = new DatabaseConnection().getMongoDatabase().getCollection(GAME_SESSION_COLLECTION_NAME);
    }

    public void addPlayerToGame(Team team) {
        Document memberDoc = new Document();
        memberDoc.put("deviceID", deviceID);
        memberDoc.put("displayName", displayName);
        memberDoc.put("isJailed", false);
        memberDoc.put("numOfCaps", 0);
        memberDoc.put("numOfTags", 0);
        memberDoc.put("numOfJails", 0);
        Document location = (Document) Utils.buildLocationDoc(this.location.latitude, this.location.longitude).get("location");
        memberDoc.put("location", location);
        memberDoc.put("items", new ArrayList<>());

        collection.findOneAndUpdate(eq("_id", 0), addToSet(team.name() + ".members", memberDoc));
    }

    public void removePlayerFromGame() {
        collection.findOneAndUpdate(eq("_id", 0), pull(Team.redTeam.name() + ".members", eq("deviceID", this.deviceID)));
        collection.findOneAndUpdate(eq("_id", 0), pull(Team.blueTeam.name() + ".members", eq("deviceID", this.deviceID)));
    }

    public ArrayList<String> getTeamMemberIDs(final Team team)
    {
        ArrayList<String> arrayList = new ArrayList<>();

        Document d = (Document) collection.find(eq("_id", 0)).projection(fields(include(team.name() + ".members.deviceID"), excludeId())).first();
        Document teamDoc = (Document) d.get(team.name());
        ArrayList<Document> deviceDocs = (ArrayList<Document>) teamDoc.get("members");

        for(Document doc : deviceDocs)
        {
            arrayList.add(doc.getString("deviceID"));
        }

       return arrayList;


    }


}
