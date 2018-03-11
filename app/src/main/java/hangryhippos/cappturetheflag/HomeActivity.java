package hangryhippos.cappturetheflag;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

public class HomeActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "HomeActivity";
    public static final String APP_NAME = "Cappture the Flag";
    public static final String DISPLAY_NAME_KEY = "DisplayName";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private boolean stopPermissionRequests;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_LEADERBOARD_UI = 9004;


    // Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);



        ImageView play = findViewById(R.id.btn_play);
        play.setOnClickListener(this);

        ImageView help = findViewById(R.id.btn_help);
        help.setOnClickListener(this);

        ImageView settings = findViewById(R.id.btn_settings);
        settings.setOnClickListener(this);

        buildGoogleApiClient();

        //Check if the player has a name stored - if not then ask them to enter one
        if (!checkDisplayName()) {
            enterDisplayName();
        }

        //Check if an internet connection is available - if not send an alert dialog.
        if (!isNetworkAvailable(this)) {
            sendNetworkErrorDialog();
        }

    }

    @Override
    protected void onResume() {
        if (!checkPermissions()) {
            requestPermissions();
        }
        super.onResume();
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
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
            case R.id.button_sign_in:
                // start the sign-in flow
                Log.d(TAG, "Sign-in button clicked");
                mSignInClicked = true;
                mGoogleApiClient.connect();
                break;
            case R.id.wLeaderboard:
                showLeaderboard();
                break;

        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected() called. Sign in successful!");

//        submitScoreToLeaderboard();
        if (findViewById(R.id.sign_in_bar).getVisibility() == View.VISIBLE) {
            hideSignInBar();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        buildGoogleApiClient();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient,
                    connectionResult, RC_SIGN_IN, getString(R.string.signin_other_error));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                    + responseCode + ", intent=" + intent);
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (responseCode == RESULT_OK) {
                buildGoogleApiClient();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, responseCode, R.string.signin_other_error);
                showSignInBar();
            }
        }
    }


    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
        buildGoogleApiClient();

//        submitScoreToLeaderboard();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }
    }

    /**
     * Method builds the Google Api Client for use by in signing in to Google Play Services
     */
    private synchronized void buildGoogleApiClient() {

            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                        .build();
            }
            mGoogleApiClient.connect();
    }

    /**
     * Shows the "sign in" bar (explanation and button).
     */
    private void showSignInBar() {
        Log.d(TAG, "Showing sign in bar");
        findViewById(R.id.sign_in_bar).setVisibility(View.VISIBLE);
    }

    /**
     * Hides the "sign in" bar (explanation and button).
     */
    private void hideSignInBar() {
        Log.d(TAG, "Showing sign in bar");
        findViewById(R.id.sign_in_bar).setVisibility(View.GONE);
    }

    /**
     * The Leaderboard Activity is started if the user has been signed in and otherwise it displays
     * the sign-in bar to allow the user to re-try their connection
     * <p>
     * Called by generalActivityIntents()
     */
    private void showLeaderboard() {

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            String LEADERBOARD_ID = getString(R.string.google_leaderboard_key);

            startActivityForResult(
                    Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, LEADERBOARD_ID),
                    RC_LEADERBOARD_UI);
        } else {
            showSignInBar();
        }
    }

    /**
     * Method submits the player's score to the leaderboard if one exists. That is if the score has
     * been set to a value other than -1 which it has been initialised to.
     * <p>
     * Only is called when the user has been signed-in on connect or on the start of the activity if
     * the user has previously been able to sign-in.
     * <p>
     * It resets the score if the user has successfully submitted theirs.
     */
    private void submitScoreToLeaderboard() {

//        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) { //Player is signed-in

//            //Get details here to submit
//            if (scoreToSubmit > 0) {
//                //Submit the score
//                if (mGoogleApiClient.isConnected()) {
//                    String LEADERBOARD_ID = getString(R.string.google_leaderboard_key);
//                    Games.Leaderboards.submitScore(mGoogleApiClient, LEADERBOARD_ID, scoreToSubmit);
//                }
//            }
//        }
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }


    //use if a network connection is required for the selected option to work
    private void sendNetworkErrorDialog() {
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
    private boolean checkDisplayName() {
        SharedPreferences settings = this.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        return settings.getString(DISPLAY_NAME_KEY, null) != null;
    }

    private void saveDisplayName(String name) {
        SharedPreferences.Editor editor = this.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(DISPLAY_NAME_KEY, name);
        editor.apply();
    }

    private void enterDisplayName() {

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
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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

    //Check that location permissions are granted
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            showLocationRationaleDialog();
        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays
                requestPermissions();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Refresh the view so that the home activity is updated.
                finish();
                startActivity(getIntent());
            } else {
                Log.d(TAG, "Permission was denied");
                // Permission denied.
                // Notify the user that they have denied a core permission
                requestPermissions();
            }
        }
    }

    private void showLocationRationaleDialog() {
        Log.d(TAG, "show location rationale dialog called");
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.location_error);
        adb.setMessage(R.string.msg_location_rationale);
        adb.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(HomeActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSIONS_REQUEST_CODE);
                dialog.dismiss();
            }
        });
        AlertDialog ad = adb.create();
        ad.setCancelable(false);
        ad.show();
    }

}

