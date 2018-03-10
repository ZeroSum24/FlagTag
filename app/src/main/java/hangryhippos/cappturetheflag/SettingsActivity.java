package hangryhippos.cappturetheflag;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {
    public String displayName;
    private String connectionType="4G";

    public String getDisplayName(){
        return displayName;
    }
    public void setDisplayName(String newname){
        displayName=newname;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
}
