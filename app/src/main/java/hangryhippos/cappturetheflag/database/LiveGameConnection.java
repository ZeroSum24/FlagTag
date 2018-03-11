package hangryhippos.cappturetheflag.database;

import com.google.android.gms.maps.model.LatLng;
import com.mongodb.client.MongoCollection;

import org.bson.Document;

import java.util.ArrayList;

import hangryhippos.cappturetheflag.database.obj.Team;
import hangryhippos.cappturetheflag.database.obj.Utils;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.addToSet;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.pull;
import static com.mongodb.client.model.Updates.set;

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
        memberDoc.put("item", null);

        collection.findOneAndUpdate(eq("_id", 0), addToSet(team.name() + ".members", memberDoc));
    }

    public void removePlayerFromGame() {
        collection.findOneAndUpdate(eq("_id", 0), pull(Team.redTeam.name() + ".members", eq("deviceID", this.deviceID)));
        collection.findOneAndUpdate(eq("_id", 0), pull(Team.blueTeam.name() + ".members", eq("deviceID", this.deviceID)));
    }

    public ArrayList<String> getTeamMemberIDs(final Team team) {
        ArrayList<String> arrayList = new ArrayList<>();

        Document d = (Document) collection.find(eq("_id", 0)).projection(fields(include(team.name() + ".members.deviceID"), excludeId())).first();
        Document teamDoc = (Document) d.get(team.name());
        ArrayList<Document> deviceDocs = (ArrayList<Document>) teamDoc.get("members");

        for (Document doc : deviceDocs) {
            arrayList.add(doc.getString("deviceID"));
        }

        return arrayList;
    }

    public void updatePlayerPos(LatLng latLng) {
        ArrayList<Double> coords = new ArrayList<>();
        coords.add(latLng.latitude);
        coords.add(latLng.longitude);
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), set("redTeam.members.$.location.coordinates", coords));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), set("blueTeam.members.$.location.coordinates", coords));
    }

    public void setJailStatus(boolean inJail) {
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), set("redTeam.members.$.isJailed", inJail));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), set("blueTeam.members.$.isJailed", inJail));
    }

    public void setNumberOfCaptures(int num) {
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), set("redTeam.members.$.numOfCaps", num));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), set("blueTeam.members.$.numOfCaps", num));
    }

    public void incrementNumberOfCaptures() {
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), inc("redTeam.members.$.numOfCaps", 1));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), inc("blueTeam.members.$.numOfCaps", 1));
    }

    public void setNumberOfTags(int num) {
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), set("redTeam.members.$.numOfTags", num));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), set("blueTeam.members.$.numOfTags", num));
    }

    public void incrementNumberOfTags() {
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), inc("redTeam.members.$.numOfTags", 1));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), inc("blueTeam.members.$.numOfTags", 1));
    }

    public void setNumberOfJails(int num) {
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), set("redTeam.members.$.numOfJails", num));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), set("blueTeam.members.$.numOfJails", num));
    }

    public void incrementNumberOfJails() {
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), inc("redTeam.members.$.numOfJails", 1));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), inc("blueTeam.members.$.numOfJails", 1));
    }

    public void setPlayerItem(String name) {
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), set("redTeam.members.$.item", name));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), set("blueTeam.members.$.item", name));
    }

    public void removeItemFromPlayer() {

        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), set("redTeam.members.$.item", null));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), set("blueTeam.members.$.item", null));
    }




}
