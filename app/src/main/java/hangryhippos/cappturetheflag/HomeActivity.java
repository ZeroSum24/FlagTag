package hangryhippos.cappturetheflag;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

public class HomeActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {
    private int currentViewID;
    private FragmentManager fragmentManager;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        fragmentManager = getSupportFragmentManager();

        drawerLayout = findViewById(R.id.drawer_layout_main);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        displayView(item.getItemId());
        return true;
    }

    // Switch between different fragment views.
    private void displayView(int viewId){
        Fragment fragment =  null;
        String title = getString(R.string.app_name);
        switch(viewId){
            case R.id.nav_home:
                fragment = new HomeFragment();
                break;

            case R.id.nav_items_list:
                fragment = new ItemsFragment();
                break;

            case R.id.nav_how_to_play:
                fragment = new HelpFragment();
                break;

            case R.id.nav_settings:
                fragment = new SettingsFragment();
                break;
        }

        if (fragment != null){
            FragmentTransaction ft = fragmentManager.beginTransaction();

            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        currentViewID = viewId;
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed(){
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (currentViewID != R.id.nav_home){
            displayView(R.id.nav_home);
        } else {
            moveTaskToBack(true);
        }
    }
}
