package hangryhippos.cappturetheflag.database.obj;

import android.support.v4.util.Pair;

import org.bson.Document;

import java.util.ArrayList;

/**
 * Created by colin on 10/03/18.
 */

public class Utils
{
    private static final String TOP_LEFT = "topLeft";
    private static final String BOTTOM_RIGHT = "bottomRight";



    public static Document buildLocationDoc(double latitude, double longitude)
    {
        ArrayList<Double> coords = new ArrayList<>();
        coords.add(latitude);
        coords.add(longitude);

        Document parent = new Document();
        Document inner = new Document();
        inner.put("type", "Point");
        inner.put("coordinates", coords);
        parent.put("location", inner);
        return parent;
    }

    public static Zone getZone(Document doc, String parentKey)
    {

        try
        {
            Document top = (Document) doc.get(parentKey);
            Document topLeft = (Document) top.get(TOP_LEFT);
            Document bottomRight = (Document) top.get(BOTTOM_RIGHT);

            ArrayList<Double> topLeftArray = (ArrayList<Double>) topLeft.get("coordinates");
            ArrayList<Double> bottomRightArray = (ArrayList<Double>) bottomRight.get("coordinates");

            Pair<Double, Double> topLeftPair = new Pair<>(topLeftArray.get(0), topLeftArray.get(1));
            Pair<Double, Double> bottomRightPair = new Pair<>(bottomRightArray.get(0), bottomRightArray.get(1));

            return new Zone(topLeftPair, bottomRightPair);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }

    }
}
