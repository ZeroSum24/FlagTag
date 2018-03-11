package hangryhippos.cappturetheflag.database.obj;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Paul on 11/03/2018.
 */

public class Item {
    private String id;
    private String name;
    private String description;
    private String item_class;
    private LatLng position;

    public Item(String id, String name, String description, String item_class, LatLng position){
        this.id = id;
        this.name = name;
        this.position = position;
        this.description = description;
        this.item_class = item_class;
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

    public String getItemClass(){
        return item_class;
    }

    public LatLng getPosition(){
        return position;
    }

}
