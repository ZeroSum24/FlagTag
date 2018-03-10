package hangryhippos.cappturetheflag.database.obj;

import android.util.Pair;

/**
 * Represents a 2D (lat/long) zone in the real world using two points of
 * a rectangle (top left and bottom right)
 */

public class Zone
{
    private Pair<Double, Double> topLeft;
    private Pair<Double, Double> bottomRight;

    public Zone(Pair<Double, Double> topLeft, Pair<Double, Double> bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }

    public Pair<Double, Double> getTopLeft() {
        return topLeft;
    }

    public Pair<Double, Double> getTopRight() {
        return bottomRight;
    }
}
