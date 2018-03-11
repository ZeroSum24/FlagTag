package hangryhippos.cappturetheflag.database.obj;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Paul on 11/03/2018.
 */

public class Item {
    private String id;
    private String name;
    private LatLng position;

    public Item(String id, String name, LatLng position){
        this.id = id;
        this.name = name;
        this.position = position;
    }

    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public LatLng getPosition(){
        return position;
    }
}
