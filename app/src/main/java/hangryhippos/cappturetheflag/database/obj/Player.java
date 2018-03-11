package hangryhippos.cappturetheflag.database.obj;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by colin on 11/03/18.
 */

public class Player
{
    private String deviceID;
    private String displayName;
    private Team team;
    private boolean isJailed;
    private int numOfCaps;
    private int numOfTags;
    private int numOfJails;
    private String item;
    private LatLng position;

    public Player(String deviceID, String displayName, Team team, boolean isJailed, int numOfCaps, int numOfTags, int numOfJails, String item, LatLng position) {
        this.deviceID = deviceID;
        this.displayName = displayName;
        this.team = team;
        this.isJailed = isJailed;
        this.numOfCaps = numOfCaps;
        this.numOfTags = numOfTags;
        this.numOfJails = numOfJails;
        this.item = item;
        this.position = position;
    }
//Don't need setters for ID/name/team as they shouldn't change mid-game
    public String getDeviceID() {
        return deviceID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Team getTeam() {
        return team;
    }

    public boolean isJailed() {
        return isJailed;
    }

    public void sendToJail(){
        isJailed = true;
    }

    public void freeFromJail(){
        isJailed = false;
    }

    public int getNumOfCaps() {
        return numOfCaps;
    }

    public void incrementNumOfCaps(){
        numOfCaps++;
    }

    public int getNumOfTags() {
        return numOfTags;
    }

    public void incrementNumOfTags(){
        numOfTags++;
    }

    public int getNumOfJails() {
        return numOfJails;
    }

    public void incrementNumOfJails(){
        numOfJails++;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String itemID){
        this.item = itemID;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position){
        this.position = position;
    }
}
