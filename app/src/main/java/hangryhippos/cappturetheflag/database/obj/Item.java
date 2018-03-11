package hangryhippos.cappturetheflag.database.obj;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Paul on 11/03/2018.
 */

public class Item {
    private String id;
    private String name;
    private String description;
    private int icon_id;
    private LatLng position;

    public Item(String id, String name, String description, int icon_id, LatLng position){
        this.id = id;
        this.name = name;
        this.position = position;
        this.description = description;
        this.icon_id = icon_id;
    }

    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public int getIcon_id(){
        return icon_id;
    }

    public LatLng getPosition(){
        return position;
    }

}
