package hangryhippos.cappturetheflag.database.obj;

import com.google.android.gms.maps.model.LatLng;

/**
 * Object representing the rule set used for creating a game instance
 */

public class RuleSet
{
    private int scoreLimit;
    private int maxPerTeam;
    private Zone redZone;
    private Zone blueZone;
    private Zone neutralZone;
    private Zone jailZone;
    private LatLng blueFlagLoc, redFlagLoc;

    public RuleSet(int scoreLimit, int maxPerTeam, Zone redZone, Zone blueZone, Zone neutralZone, Zone jailZone, LatLng blueFlagLoc, LatLng redFlagLoc) {
        this.scoreLimit = scoreLimit;
        this.maxPerTeam = maxPerTeam;
        this.redZone = redZone;
        this.blueZone = blueZone;
        this.neutralZone = neutralZone;
        this.jailZone = jailZone;
        this.blueFlagLoc = blueFlagLoc;
        this.redFlagLoc = redFlagLoc;
    }

    public int getScoreLimit() {
        return scoreLimit;
    }

    public int getMaxPerTeam() {
        return maxPerTeam;
    }

    public Zone getRedZone() {
        return redZone;
    }

    public Zone getBlueZone() {
        return blueZone;
    }

    public Zone getNeutralZone() {
        return neutralZone;
    }

    public Zone getJailZone() {
        return jailZone;
    }

    public LatLng getBlueFlagLoc() {
        return blueFlagLoc;
    }

    public LatLng getRedFlagLoc() {
        return redFlagLoc;
    }

    public String toString()
    {
        return String.format("Score Limit: %d\nMax Per Team: %d\nBlue Zone: %s\nRed Zone: %s\nNeutral Zone: %s\nJail Zone: %s",
        this.getScoreLimit(), this.getMaxPerTeam(), this.getBlueZone().toString(), this.getRedZone().toString(), this.getNeutralZone().toString(), this.getJailZone().toString());
    }
}
