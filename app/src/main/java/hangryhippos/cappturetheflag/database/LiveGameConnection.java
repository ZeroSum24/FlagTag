package hangryhippos.cappturetheflag.database;

import com.google.android.gms.maps.model.LatLng;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;

import org.bson.Document;

import java.util.ArrayList;

import hangryhippos.cappturetheflag.database.obj.Player;
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


    public ArrayList<Player> getPlayers()
    {
        final ArrayList<Player> players = new ArrayList<>();

        Block<Document> printBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                Document redTeamDoc = (Document) document.get(Team.redTeam.name());
                ArrayList<Document> redArray = (ArrayList<Document>) redTeamDoc.get("members");

                for(Document doc : redArray)
                {
                    players.add(genPlayer(doc, Team.redTeam));
                }

                Document blueTeamDoc = (Document) document.get(Team.blueTeam.name());
                ArrayList<Document> blueArray = (ArrayList<Document>) blueTeamDoc.get("members");

                for(Document doc : blueArray)
                {
                    players.add(genPlayer(doc, Team.blueTeam));
                }

            }
        };

        collection.find(eq("_id", 0)).forEach(printBlock);

        return players;
    }

    private Player genPlayer(Document doc, Team team)
    {
        String deviceID = doc.getString("deviceID");
        String displayName = doc.getString("displayName");
        boolean isJailed = doc.getBoolean("isJailed");
        int numOfCaps = doc.getInteger("numOfCaps");
        int numOfTags = doc.getInteger("numOfTags");
        int numOfJails = doc.getInteger("numOfJails");
        String item = doc.getString("item");

        Document loc = (Document) doc.get("location");
        ArrayList<Double> coords = (ArrayList<Double>) loc.get("coordinates");
        LatLng pos = new LatLng(coords.get(0), coords.get(1));

        return new Player(deviceID, displayName, team, isJailed, numOfCaps, numOfTags, numOfJails, item, pos);
    }



}
