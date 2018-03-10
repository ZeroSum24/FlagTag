package hangryhippos.cappturetheflag;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class HomeActivity extends AppCompatActivity implements
        View.OnClickListener{
    private int currentViewID;
    private FragmentManager fragmentManager;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_play){
            Intent playIntent = new Intent(this, PlayActivity.class);
            startActivity(playIntent);
        }

        if (v.getId() == R.id.btn_help){
            Intent helpIntent = new Intent(this, HelpActivity.class);
            startActivity(helpIntent);
        }
        if (v.getId() == R.id.btn_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
    }
}
