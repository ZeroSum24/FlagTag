package hangryhippos.cappturetheflag;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class HomeActivity extends AppCompatActivity implements
        View.OnClickListener{

    public static final String APP_NAME = "Cappture the Flag";
    public static final String DISPLAY_NAME_KEY = "DisplayName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        ImageView play = findViewById(R.id.btn_play);
        play.setOnClickListener(this);

        ImageView help = findViewById(R.id.btn_help);
        help.setOnClickListener(this);

        ImageView settings = findViewById(R.id.btn_settings);
        settings.setOnClickListener(this);

        //Check if the player has a name stored - if not then ask them to enter one
        if (!checkDisplayName()){
            enterDisplayName();
        }

        //Check if an internet connection is available - if not send an alert dialog.
        if (!isNetworkAvailable(this)){
            sendNetworkErrorDialog();
        }

    }


    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.btn_play:
                Intent playIntent = new Intent(this, PlayActivity.class);
                startActivity(playIntent);
                break;
            case R.id.btn_help:
                Intent helpIntent = new Intent(this, HelpActivity.class);
                startActivity(helpIntent);
                break;
            case R.id.btn_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;


        }
    }

    private boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }


    //use if a network connection is required for the selected option to work
    private void sendNetworkErrorDialog(){
        //with no internet, send an alert that will take user to settings or close the app.
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.network_error);
        adb.setMessage(R.string.msg_data_required);
        adb.setPositiveButton(R.string.internet_settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
        });
        adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog ad = adb.create();
        ad.show();
    }

    // Checks if a display name is stored
    // Returns true if a name is stored, false otherwise
    private boolean checkDisplayName(){
        SharedPreferences settings = this.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        return settings.getString(DISPLAY_NAME_KEY, null) != null;
    }

    private void saveDisplayName(String name){
        SharedPreferences.Editor editor = this.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(DISPLAY_NAME_KEY, name);
        editor.apply();
    }

    private void enterDisplayName(){

        final EditText input = new EditText(this);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.enter_name)
                .setMessage(R.string.msg_enter_name)
                .setView(input)
                .setPositiveButton(R.string.okay, null)
                .create();

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button b = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        String displayName = input.getText().toString();
                        //Checks that a string has been entered
                        if (displayName.length() > 0) {
                            saveDisplayName(displayName);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        dialog.show();
    }

}
