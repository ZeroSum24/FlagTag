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

    /**
     * Add player to game
     * @param team Team for user to be join
     */
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

    /**
     * Removes user from members list respective team
     */
    public void removePlayerFromGame() {
        collection.findOneAndUpdate(eq("_id", 0), pull(Team.redTeam.name() + ".members", eq("deviceID", this.deviceID)));
        collection.findOneAndUpdate(eq("_id", 0), pull(Team.blueTeam.name() + ".members", eq("deviceID", this.deviceID)));
    }

    /**
     * Update the player's position
     * @param latLng New position
     */
    public void updatePlayerPos(LatLng latLng) {
        ArrayList<Double> coords = new ArrayList<>();
        coords.add(latLng.latitude);
        coords.add(latLng.longitude);
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), set("redTeam.members.$.location.coordinates", coords));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), set("blueTeam.members.$.location.coordinates", coords));
    }

    /**
     * Set whether the user is in jail or not
     * @param inJail True if in jail, otherwise
     */
    public void setJailStatus(boolean inJail) {
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), set("redTeam.members.$.isJailed", inJail));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), set("blueTeam.members.$.isJailed", inJail));
    }


    /**
     * Set the number of flag captures the user has
     * @param num Number of flag captures
     */
    public void setNumberOfCaptures(int num) {
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), set("redTeam.members.$.numOfCaps", num));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), set("blueTeam.members.$.numOfCaps", num));
    }

    /**
     * Increment the user's number of flag captures by one
     */
    public void incrementNumberOfCaptures() {
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), inc("redTeam.members.$.numOfCaps", 1));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), inc("blueTeam.members.$.numOfCaps", 1));
    }

    /**
     * Set the number of players the user has tagged
     * @param num Number of players tagged
     */
    public void setNumberOfTags(int num) {
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), set("redTeam.members.$.numOfTags", num));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), set("blueTeam.members.$.numOfTags", num));
    }

    /**
     * Increment the user's tag count by one
     */
    public void incrementNumberOfTags() {
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), inc("redTeam.members.$.numOfTags", 1));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), inc("blueTeam.members.$.numOfTags", 1));
    }

    /**
     * Set the number of times the user has been put in jail
     * @param num Number of times in jail
     */
    public void setNumberOfJails(int num) {
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), set("redTeam.members.$.numOfJails", num));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), set("blueTeam.members.$.numOfJails", num));
    }

    /**
     * Increment the number of times the user has been in jail by one
     */
    public void incrementNumberOfJails() {
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), inc("redTeam.members.$.numOfJails", 1));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), inc("blueTeam.members.$.numOfJails", 1));
    }



    /**
     * Set the item the player is currently holding
     * @param name Name of item
     */
    public void setPlayerItem(String name) {
        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), set("redTeam.members.$.item", name));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), set("blueTeam.members.$.item", name));
    }

    /**
     * Remove the current item from the player
     */
    public void removeItemFromPlayer() {

        collection.findOneAndUpdate(and(eq("_id", 0), eq("redTeam.members.deviceID", deviceID)), set("redTeam.members.$.item", null));
        collection.findOneAndUpdate(and(eq("_id", 0), eq("blueTeam.members.deviceID", deviceID)), set("blueTeam.members.$.item", null));
    }

    /**
     * Retrieve the list of players currently in the game session
     * @return List of current players
     */
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

    /**
     * Private method for generating a player object from a member BSON object
     * @param doc The BSON document representing a "member"
     * @param team The team the player is on
     * @return Player object
     */
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

    /**
     * Set whether game is in progress or not
     * @param inProgress Whether game is in progress or not
     */
    public void setGameInProgress(boolean inProgress)
    {
        collection.findOneAndUpdate(eq("_id", 0), set("in_progress", inProgress));
    }

    /**
     * Update team's flag pos
     * @param team Team's flag to update
     * @param latLng New pos
     */
    public void updateFlagPos(Team team, LatLng latLng)
    {

        ArrayList<Double> coords = new ArrayList<>();
        coords.add(latLng.latitude);
        coords.add(latLng.longitude);

        if(team == Team.blueTeam)
        {
            collection.findOneAndUpdate(eq("_id", 0), set("blueFlag.location.coordinates", coords));
        }
        else
        {
            collection.findOneAndUpdate(eq("_id", 0), set("redFlag.location.coordinates", coords));
        }
    }

    /**
     * Get position of a team's flag
     * @param team Team's flag to get position of
     * @return LatLng of flag
     */
    public LatLng getFlagPos(Team team)
    {
        if(team == Team.blueTeam)
        {
            Document parent = (Document) collection.find(eq("_id", 0)).first();
            Document flag = (Document) parent.get("blueFlag");
            Document loc = (Document) flag.get("location");
            ArrayList<Double> coords = (ArrayList<Double>) loc.get("coordinates");
            LatLng pos = new LatLng(coords.get(0), coords.get(1));
            return pos;
        }
        else
        {
            Document parent = (Document) collection.find(eq("_id", 0)).first();
            Document flag = (Document) parent.get("redFlag");
            Document loc = (Document) flag.get("location");
            ArrayList<Double> coords = (ArrayList<Double>) loc.get("coordinates");
            LatLng pos = new LatLng(coords.get(0), coords.get(1));
            return pos;
        }
    }



}
