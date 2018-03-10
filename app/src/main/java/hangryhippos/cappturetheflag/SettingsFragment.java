package hangryhippos.cappturetheflag;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by CESha on 10/03/2018.
 */

public class SettingsFragment extends Fragment{
    private String displayName;
    private Activity activity;
    private String connectionType="4G";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }
    public String getDisplayName(){
        return displayName;
    }
    public void setDisplayName(String newname){
        displayName=newname;
    }


}
