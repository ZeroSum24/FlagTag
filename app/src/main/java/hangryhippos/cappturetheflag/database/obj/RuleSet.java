package hangryhippos.cappturetheflag.database.obj;

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

    public RuleSet(int scoreLimit, int maxPerTeam, Zone redZone, Zone blueZone, Zone neutralZone, Zone jailZone) {
        this.scoreLimit = scoreLimit;
        this.maxPerTeam = maxPerTeam;
        this.redZone = redZone;
        this.blueZone = blueZone;
        this.neutralZone = neutralZone;
        this.jailZone = jailZone;
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
}
