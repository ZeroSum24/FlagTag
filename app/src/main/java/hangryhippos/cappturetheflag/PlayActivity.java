package hangryhippos.cappturetheflag;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.location.Location;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.os.Process;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Game;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.SphericalUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import hangryhippos.cappturetheflag.database.GameCreatorConnection;
import hangryhippos.cappturetheflag.database.LiveGameConnection;
import hangryhippos.cappturetheflag.database.obj.Item;
import hangryhippos.cappturetheflag.database.obj.Player;
import hangryhippos.cappturetheflag.database.obj.Team;

public class PlayActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        CreateNdefMessageCallback {


    public NfcAdapter mNfcAdapter;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = false;
    private Location mLastLocation;
    private static final String TAG = "MapsActivity";
    private TextView textView;
    private List<Marker> markerList;

    // Bounds for area intially
    private static final LatLngBounds EDINBURGH_MEADOWS =
            new LatLngBounds(new LatLng(55.940671, -3.193070), new LatLng(55.941288, -3.189714));

    // area where team 1 respawns
    private LatLngBounds respawnZoneBlue;
    private LatLngBounds respawnZoneRed;
    // team 1's area in general (covers respawn area too to make tagging easier)
    private LatLngBounds zoneBlue;
    private LatLngBounds zoneRed;
    private LatLngBounds neutralZone;
    // Gives the respawn area for the player's team
    // NB different to the respawn area for two teams as it changes depending on your team
    private LatLngBounds respawnArea;
    // Gives the last location of the player
    private Location playerLocation;
    // Indicates if the player has been tagged by an enemy
    private boolean playerTagged;
    // Shows the progress of the countdown
    private ProgressBar countdownProgress;
    // Length of time (in milliseconds that players have to wait to get released
    private int waitingTime = 20000;
    //Number of times location can be updated and not trigger a dialog if it's out of bounds
    private int outOfBoundsGrace = 50;
    private CountDownTimer countDownTimer;
    private enum TimerStatus {
        STARTED,
        STOPPED
    }
    private TimerStatus timerStatus = TimerStatus.STOPPED;

    // True if enemy flag has been revealed; false otherwise
    private boolean flagRevealed;

    // True if enemy is shown on map; false otherwise
    // Separate as there may be time between revealing flag and showing on map
    // Can avoid replacing the flag on the map repeatedly.
    private boolean flagVisible;

    // True if player is holding the flag; false otherwise
    private boolean holdingFlag;

    // True if the game is in progress for a round; false if there is a break e.g. to reset
    private boolean gamePlayingNormally;

    private Player player;
    private Team playerTeam;

    private ArrayList<LatLng> blueFlagLocations;
    private ArrayList<LatLng> redFlagLocations;

    // Distance user must be from a flag to locate it
    private static final int FLAG_DISTANCE = 20;

    // Distance user must be from an item/flag to pick it up
    private static final int PICKUP_DISTANCE = 5;

    private LatLng blueFlagLocation;
    private LatLng redFlagLocation;

    // all items on the map. Needs to be refreshed
    private ArrayList<Item> items;

    private int blueScore = 0;
    private int redScore = 0;

    private LiveGameConnection gameConnection;

    private enum GameMode{
        normal,
        tagged,
        reset,
        ended
    }

    private GameMode currentGameMode = GameMode.normal;

    private BitmapDescriptor ic_red_flag;
    private BitmapDescriptor ic_blue_flag;
    private BitmapDescriptor ic_common_item;
    private BitmapDescriptor ic_rare_item;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        blueScore = 0;
        redScore = 0;
        //TODO get scores from server
        displayScores();

        initialisePlayer();
        //TODO get flag lat/lngs from server - need to also refresh from server

        ic_red_flag = BitmapDescriptorFactory.fromResource(R.drawable.ic_red_flag);
        ic_blue_flag = BitmapDescriptorFactory.fromResource(R.drawable.ic_blue_flag);
        ic_common_item = BitmapDescriptorFactory.fromResource(R.drawable.ic_item_normal);
        ic_rare_item = BitmapDescriptorFactory.fromResource(R.drawable.ic_item_rare);


        //Initialises Difficulty and Points Systems
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
        }
        else {
            String text = ("tag, you're it!");
            NdefMessage msg = new NdefMessage(
                    new NdefRecord[]{createMimeRecord(
                            "application/com.example.android.beam", text.getBytes())
                            /**
                             * The Android Application Record (AAR) is commented out. When a device
                             * receives a push with an AAR in it, the application specified in the AAR
                             * is guaranteed to run. The AAR overrides the tag dispatch system.
                             * You can add it back in to guarantee that this
                             * activity starts when receiving a beamed message. For now, this code
                             * uses the tag dispatch system.
                            */
                            //,NdefRecord.createApplicationRecord("com.example.android.beam")
                    });
            mNfcAdapter.setNdefPushMessageCallback(this, this);
        }
        Log.e("MapsActivityCreate", "DifficultyLevel level: ");

        //Obtain the SupportMapFragment
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);

        
        //Get notified when the map is ready to be used. Long-running activities are performed asynchronously
        //in order to keep the user interface responsive
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }

        Log.e("LocationAPICreate", "LocationAPI null: " + (mGoogleApiClient == null));
        Log.e("LocationOnCreate", "Location null: " + (mLastLocation == null));


    }

    private void displayScores(){
        TextView tvBlueScore = findViewById(R.id.txt_view_blue_score);
        TextView tvRedScore = findViewById(R.id.txt_view_red_score);
        String formatScore = getString(R.string.format_score);
        String strBlueScore = String.format(formatScore, String.valueOf(blueScore));
        String strRedScore = String.format(formatScore, String.valueOf(redScore));
        tvBlueScore.setText(strBlueScore);
        tvRedScore.setText(strRedScore);
    }

    private void initialisePlayer(){
        Bundle bundle = getIntent().getExtras();
        String deviceID = bundle.getString(getString(R.string.device_id));
        String playerName = bundle.getString(getString(R.string.display_name));
        String jsonTeam = bundle.getString(getString(R.string.team));
        Gson gson = new Gson();
        Type type = new TypeToken<Team>(){}.getType();
        playerTeam = gson.fromJson(jsonTeam, type);

        boolean inProgress = bundle.getBoolean("newGame");
        String uuid = UUID.randomUUID().toString();
        String displayName = "HHHHHHH";

        if(!inProgress) {
            new GameCreatorConnection(extractLatLngFromLocation(playerLocation), uuid, displayName).registerNewGame(blueFlagLocations.get(0), redFlagLocations.get(0));
        }

        gameConnection = new LiveGameConnection(extractLatLngFromLocation(playerLocation),UUID.randomUUID().toString(),displayName);

        if(inProgress)
            gameConnection.addPlayerToGame(Team.redTeam);

        //TODO position is null to start with - fair enough?
        player = new Player(deviceID, playerName, playerTeam, false, 0, 0,
                0, getString(R.string.empty), null);
    }


    /**
     * Method updates the activity when the map is ready by placing all of the word markers on the
     * map. The method also updates various User Interface elements of the map
     *
     * @param googleMap the current map used which is provided by google
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(16.0f);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));

        // Constrain the camera target to the Adelaide bounds.
//        mMap.setLatLngBoundsForCameraTarget(EDINBURGH_MEADOWS);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(EDINBURGH_MEADOWS.getCenter())
                .zoom(18)
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        try {
            // Visualise current position with a small blue circle
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException se) {
            System.out.println("Security exception thrown [onMapReady]");
        }
        // Add ‘‘My location’’ button to the user interface
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        repositionMapLocationButton(); //changes the position of map location button is created
        //TODO would be good if creator of game could set bounds themselves.
        calculateZones(EDINBURGH_MEADOWS);
        addZones();
    }

    /**
     * Method repositions the map location button so that it is not overlapped by the points
     * system.
     *
     * Called in OnMapReady()
     */
    private void repositionMapLocationButton() {
        //Code to re-position the user location button
        View mapView = mapFragment.getView();
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent())
                    .findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 80, 40);
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        Log.e("MapUsingStart", "StartIsActive: " + true);
        if (this.mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("MapUsingStop", "StopIsActive: " + true);
        if (mGoogleApiClient.isConnected() && this.mGoogleApiClient != null) {
            this.mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
        switch(currentGameMode) {
            case normal:
                normalLoop();
                break;
            case tagged:
                taggedLoop();
                break;
            case reset:
                break;
            case ended:
                break;
        }
    }

    protected void createLocationRequest(int priority) {
        // Set the parameters for the location request
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); // preferably every 5 seconds
        mLocationRequest.setFastestInterval(1000); // at most every second
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Can we access the user’s current location?
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    /**
     * Method builds the Google Api Client for use by the map
     */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        try {
            createLocationRequest(LocationRequest.PRIORITY_HIGH_ACCURACY);
        } catch (java.lang.IllegalStateException ise) {
            System.out.println("IllegalStateException thrown [onConnected]");
        }
        // Can we access the user’s current location?
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            Log.e("MapLocationOn:", "Location Available:" + isLocationEnabled());
            if (!isLocationEnabled()||!isLocationModeHighPriority()) {
                checkLocationConnection(connectionHint);
            } else {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                Log.e("LocationOnConnected", "Location null: " + (mLastLocation == null));
                Log.e("LocationAPIConnected", "LocationAPI null: " + (mGoogleApiClient == null));
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Method updates the activity when the user's current location intersects with a map marker.
     * If a marker and the map intersects, the method displays a congratulations overlay and
     * removes the marker from the map
     *
     * @param current the user's current location
     */
    @Override
    public void onLocationChanged(Location current) {
        //update location of player
        //TODO update server of new location
        playerLocation = current;


        Log.d("New Location", "Lat: " + current.getLatitude() + "Lng : " + current.getLongitude());
        Location target = new Location("target");

        //If player is out of bounds, allow them to go out a bit (may be a mistake)
        if (playerOutOfBounds()){
            if (outOfBoundsGrace > 0) outOfBoundsGrace--;
            // has gone far/repeatedly out of bounds - send dialog warning.
            else sendOutOfBoundsDialog();
        }
        // If the player is carrying the flag, update the real flag's location.
        if (holdingFlag){
            LatLng newFlagLocation = extractLatLngFromLocation(playerLocation);
            if (playerTeam.equals(Team.blueTeam)){
                blueFlagLocations.set(0, newFlagLocation);
            } else {
                redFlagLocations.set(0, newFlagLocation);
            }
        }

    }


    @Override
    public void onConnectionSuspended(int flag) {
        System.out.println(" >>>> onConnectionSuspended");
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String text = ("tag, you're it!");
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMimeRecord(
                        "application/hangryhippos.cappturetheflag", text.getBytes())
                        /**
                         * The Android Application Record (AAR) is commented out. When a device
                         * receives a push with an AAR in it, the application specified in the AAR
                         * is guaranteed to run. The AAR overrides the tag dispatch system.
                         * You can add it back in to guarantee that this
                         * activity starts when receiving a beamed message. For now, this code
                         * uses the tag dispatch system.
                        */
                        ,NdefRecord.createApplicationRecord("hangryhippos.cappturetheflag")
                });
        return msg;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        /* An unresolvable error has occurred and a connection to Google APIs
         * could not be established. Display an error message, or handle
         * the failure silently
         */
        System.out.println(" >>>> onConnectionFailed");
    }


    private void normalLoop(){
        while (gamePlayingNormally)
            if (playerTagged){
                break;
            }
            if (flagRevealed && !flagVisible){
                showFlagOnMap();
            } else if (!flagRevealed && flagVisible){
                hideFlagOnMap();
            }
            //TODO only checks if close - no time limit to wait atm
            if (closeToFlag().equals(getString(R.string.real_flag)) && !holdingFlag){
                flagRevealed = true;
                holdingFlag = true;
                alertFlagPickedUp();
            } else if (closeToFlag().equals(getString(R.string.fake_flag)) && !holdingFlag){
                //TODO make it a notification?
                Toast.makeText(this, R.string.msg_fake_flag, Toast.LENGTH_SHORT).show();
            }
            String closeItemId = closeToItem();
            // If player is close to an item and has no item, get it
            if (!closeItemId.equals("") && player.getItem().equals(getString(R.string.empty))){
                pickUpItem(closeItemId);
                removeItemFromMap();
            }

            if (holdingFlag && playerInTeamZone()){
                player.incrementNumOfCaps();
                Toast.makeText(this, R.string.msg_point_friendly, Toast.LENGTH_SHORT).show();
                //TODO keep track of score and start round again
                //TODO update server that player scored
                // Both teams should have to be in their respawn areas.
                restartRound();
            }
            updateFromServer();
            //TODO check server to see if the gameplay is paused/reset
        if (playerTagged) taggedLoop();
    }

    private void taggedLoop(){
        ConstraintLayout taggedLayout = findViewById(R.id.tagged_layout);
        taggedLayout.setVisibility(View.VISIBLE);
        //TODO set any buttons floating to GONE
        initCountdownValues();
        while (playerTagged){
            if (!playerInRespawnArea()){
                stopCountDownTimer();
            } else if (timerStatus == TimerStatus.STOPPED){
                startCountDownTimer();
            }
        }
        //break out of the loop, hide the tagged layout again
        taggedLayout.setVisibility(View.GONE);

    }

    private void updateFromServer(){

    }

    private void restartRound(){

    }

    private void alertFlagPickedUp(){

    }

    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        Toast.makeText(this, (msg.getRecords()[0].getPayload().toString()), Toast.LENGTH_SHORT).show();
        if (((msg.getRecords()[0].getPayload())).toString().equals("tag, you're it!")){
            Toast.makeText(this, (msg.getRecords()[0].getPayload().toString()), Toast.LENGTH_SHORT).show();
        }
        // record 0 contains the MIME type, record 1 is the AAR, if present
    }
    /**
     * Method displays an alertManager to prompt the user to establish the location connection if it not
     * established or is not in high priority mode.The alertManager persists until the user has acted on
     * the prompt.
     * <p>
     * If the location is established and is not in high priority mode another alertManager will be
     * created on accessing the activity.
     * <p>
     * Called by onConnected after permissions have been granted()
     */
    private void checkLocationConnection(final Bundle connectionHint) {
        /* Checks Map network connections -- they do not process until both location connections has
        been completed */
//
//        if (!isLocationEnabled()) {
//            //Check if Location present, if not:
//            alertManager.showAlertDialog(MapsActivity.this, getString(R.string.mLocationHeader),
//                    getString(R.string.mLocationMessage), getString(R.string.retry),
//                    false, new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (isLocationEnabled()) {
//                                alertManager.dismissAlertDialog();
//                                onConnected(connectionHint);
//                            }
//                        }
//                    });
//        } else if (!isLocationModeHighPriority()) {
//            //Check if Location is in High Priority Mode, if not:
//            alertManager.showAlertDialog(MapsActivity.this, getString(R.string.mLocationHPErrorHeader),
//                    getString(R.string.mLocationHPErrorMessage), getString(R.string.retry),
//                    false, new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (isLocationModeHighPriority()) {
//                                alertManager.dismissAlertDialog();
//                            }
//                        }
//                    });
//        }
    }
    /**
     * Creates a custom MIME type encapsulated in an NDEF record
     *
     * @param mimeType
     */
    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
    }

    /**
     * Method checks if the location is enabled.
     * <p>
     * Called in checkLocationConnection()
     */
    private boolean isLocationEnabled() {
        //Method for checking the location connection
        int locationMode = 0;
        try {
            locationMode = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return (locationMode != Settings.Secure.LOCATION_MODE_OFF);
        // check if enabled
    }

    /**
     * Method checks if the location mode is set to high priority
     * <p>
     * Called in checkLocationConnection()
     */
    private boolean isLocationModeHighPriority() {
        /* Checks the System settings to check if the Location Mode is both on and set to
         High_Accuracy Mode. Otherwise Location may not display */
        int locationMode = 0;
        try {
            locationMode = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return (locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);

    }

    private LatLng extractLatLngFromLocation(Location location){
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    // Check if the player is in their own respawn area.
    private boolean playerInRespawnArea(){
        if(playerLocation != null){
            LatLng playerLatLng = extractLatLngFromLocation(playerLocation);
            if (playerTeam.equals(Team.blueTeam)){
                return respawnZoneBlue.contains(playerLatLng);
            } else {
                return respawnZoneRed.contains(playerLatLng);
            }
        } else {
            return false;
        }
    }

    private boolean playerInTeamZone(){
        if (playerLocation != null){
            LatLng playerLatLng = extractLatLngFromLocation(playerLocation);
            // Blue team = team 1
            if (playerTeam.equals(Team.blueTeam)){
                return zoneBlue.contains(playerLatLng);
            } else {
                return zoneRed.contains(playerLatLng);
            }
        } else {
            return false;
        }
    }

    // Check if the player has gone out of bounds.
    private boolean playerOutOfBounds(){
        if (playerLocation != null){
            LatLng playerLatLng = extractLatLngFromLocation(playerLocation);
            return EDINBURGH_MEADOWS.contains(playerLatLng);
        } else {
            return false;
        }
    }

    private void sendOutOfBoundsDialog(){
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.out_of_bounds);
        adb.setMessage(R.string.msg_out_of_bounds);
        adb.setPositiveButton(R.string.return_back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        adb.setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Quit, return to home screen
                Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(homeIntent);
            }
        });

        AlertDialog ad = adb.create();
        ad.setCancelable(false);
        ad.setCanceledOnTouchOutside(false);
        ad.show();
        // Reset the amount the player can be out of bounds.
        outOfBoundsGrace = 50;
    }

    // Initialise variables before counting down
    private void initCountdownValues(){
        countdownProgress = (ProgressBar) findViewById(R.id.progress_countdown);
        countdownProgress.setMax(waitingTime);
    }

    //TODO - need to tell it to start when the player is tagged
    //TODO - need to call initvalues()

    private void startCountDownTimer(){
        timerStatus = TimerStatus.STARTED;
        countdownProgress.setProgress(0);
        hideRespawnErrorMessage();
        countDownTimer = new CountDownTimer(waitingTime, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdownProgress.setProgress(waitingTime - (int) millisUntilFinished);
            }

            @Override
            public void onFinish() {
                countdownProgress.setProgress(waitingTime);
                playerTagged = false;
                //TODO - start normal loop again
            }
        }.start();
        countDownTimer.start();
    }

    private void stopCountDownTimer(){
        timerStatus = TimerStatus.STOPPED;
        showRespawnErrorMessage();
        countDownTimer.cancel();
    }

    private void hideRespawnErrorMessage(){
        TextView title = findViewById(R.id.txt_view_respawn_error_title);
        TextView msg = findViewById(R.id.txt_view_respawn_error_message);
        title.setVisibility(TextView.INVISIBLE);
        msg.setVisibility(TextView.INVISIBLE);
    }

    private void showRespawnErrorMessage(){
        TextView title = findViewById(R.id.txt_view_respawn_error_title);
        TextView msg = findViewById(R.id.txt_view_respawn_error_message);
        title.setVisibility(TextView.VISIBLE);
        msg.setVisibility(TextView.VISIBLE);
    }

    private void calculateZones(LatLngBounds totalArea){
        LatLng northeast = totalArea.northeast;
        LatLng southwest = totalArea.southwest;

        //TODO allow changes in orientation (not simply horizontal as needed here for simple solution)
        //x1, x6 is the order they'd appear on an axis (similarly for y1, y2)
        double x1 = southwest.longitude;
        double x6 = northeast.longitude;
        double y1 = southwest.latitude;
        double y2 = northeast.latitude;

        //TODO biased to Edinburgh here (longitude would be negative)
        double x = x6 - x1;
        double x2 = x1 + x/8;
        double x3 = x1 + x*3/8;
        double x4 = x1 + x*5/8;
        double x5 = x1 + x*7/8;



        respawnZoneBlue = new LatLngBounds(new LatLng(x1, y1), new LatLng(x2, y2));
        zoneBlue = new LatLngBounds(new LatLng(x1, y1), new LatLng(x3, y2));
        neutralZone = new LatLngBounds(new LatLng(x3, y1), new LatLng(x4, y2));
        zoneRed = new LatLngBounds(new LatLng(x4, y1), new LatLng(x5, y2));
        respawnZoneRed = new LatLngBounds(new LatLng(x5, y1), new LatLng(x6, y2));

    }

    //TODO would be better if didn't show enemy respawn zone (unnecessary)
    //Adds zones to the map and displays them. Called only when loading map for first time in the activity
    private void addZones(){
        // Check that the zones have been calculated, if not then stop
        if (respawnZoneBlue == null || zoneBlue == null
                || neutralZone == null || zoneRed == null || respawnZoneRed == null
                ) return;
        // Remove anything that might have been on the map before
        mMap.clear();
        Polygon rectRespawnTeam1 = mMap.addPolygon(calculateRectangle(respawnZoneBlue));
        rectRespawnTeam1.setTag(getString(R.string.respawn_1));
        rectRespawnTeam1.setZIndex(1);
        rectRespawnTeam1.setFillColor(0x7F2196F3);
        rectRespawnTeam1.setStrokeWidth(0);

        Polygon rectTeam1 = mMap.addPolygon(calculateRectangle(zoneBlue));
        rectTeam1.setTag(getString(R.string.zone_1));
        rectTeam1.setFillColor(0x7F1565C0);
        rectTeam1.setStrokeWidth(0);

        Polygon rectNeutral = mMap.addPolygon(calculateRectangle(neutralZone));
        rectNeutral.setTag(getString(R.string.zone_neutral));
        rectNeutral.setFillColor(0x7F9E9E9E);
        rectNeutral.setStrokeWidth(0);

        Polygon rectTeam2 = mMap.addPolygon(calculateRectangle(zoneRed));
        rectTeam2.setTag(getString(R.string.zone_2));
        rectTeam2.setFillColor(0x7FC62828);
        rectTeam2.setStrokeWidth(0);

        Polygon rectRespawnTeam2 = mMap.addPolygon(calculateRectangle(respawnZoneRed));
        rectRespawnTeam2.setTag(getString(R.string.respawn_2));
        rectRespawnTeam2.setZIndex(1);
        rectRespawnTeam2.setFillColor(0x7FF44336);
        rectRespawnTeam2.setStrokeWidth(0);

    }
    //TODO assumes rectangle is horizontal
    private PolygonOptions calculateRectangle(LatLngBounds bounds){
        LatLng northeast = bounds.northeast;
        LatLng southwest = bounds.southwest;
        double x1 = southwest.longitude;
        double x2 = northeast.longitude;
        double y1 = southwest.latitude;
        double y2 = northeast.latitude;

        return new PolygonOptions()
                .clickable(true)
                .add(new LatLng(x1, y1),
                     new LatLng(x1, y2),
                     new LatLng(x2, y2),
                     new LatLng(x2, y1));
    }

    private void addItems(){
        for (Item item : items){
            LatLng itemPosition = item.getPosition();
            String itemClass = item.getItemClass();
            if (itemClass.equals(getString(R.string.common))){
                mMap.addMarker(new MarkerOptions().position(itemPosition).icon(ic_common_item));
            } else {
                mMap.addMarker(new MarkerOptions().position(itemPosition).icon(ic_rare_item));
            }
        }
    }


    private void refreshMap(){
        mMap.clear();
        addZones();
        addItems();
        if (flagVisible) showFlagOnMap();
    }

    private void showFlagOnMap(){
        if (playerTeam.equals(Team.blueTeam)){
            // Shows all flags (there may be more than one due to dummy flags)
            // True flag is at index 0.
            // TODO use ArrayList for flagVisible - could hide dummy flags. Temp solution = send Toast
            for(LatLng flagLocation : redFlagLocations){
                mMap.addMarker(new MarkerOptions().position(flagLocation).icon(ic_red_flag));
            }
        } else {
            for(LatLng flagLocation : blueFlagLocations){
                mMap.addMarker(new MarkerOptions().position(flagLocation).icon(ic_blue_flag));
            }
        }
        flagVisible = true;
    }

    private void hideFlagOnMap(){
        flagVisible = false;
        refreshMap();
    }

    // Returns if the person is next to a) no flag, b) a fake flag, c) the real flag
    private String closeToFlag(){
        if (playerLocation != null){
            LatLng playerLatLng = extractLatLngFromLocation(playerLocation);
            // Blue team = team 1
            if (playerTeam.equals(Team.blueTeam)){
                for(int i = 0; i < redFlagLocations.size(); i++){
                    double distance = SphericalUtil.computeDistanceBetween(redFlagLocations.get(i), playerLatLng);
                    if (distance < PICKUP_DISTANCE){
                        // Close to the real flag
                        if (i == 0) return getString(R.string.real_flag);
                        // Close to a flag that is fake
                        else return getString(R.string.fake_flag);
                    }
                    if (distance < FLAG_DISTANCE){
                        // Notify user that a flag is nearby
                        return getString(R.string.flag_close);
                    }
                }
                // finished loop, found no nearby flags - return no flag
                return getString(R.string.no_flag);

            } else {//player is on red team
                for(int i = 0; i < blueFlagLocations.size(); i++){
                    double distance = SphericalUtil.computeDistanceBetween(blueFlagLocations.get(i), playerLatLng);
                    if (distance < PICKUP_DISTANCE){
                        // Close to the real flag
                        if (i == 0) return getString(R.string.real_flag);
                            // Close to a flag that is fake
                        else return getString(R.string.fake_flag);
                    }
                    if (distance < FLAG_DISTANCE){
                        return getString(R.string.flag_close);
                    }
                }
                return getString(R.string.no_flag);
            }
        } else {
            return getString(R.string.no_flag);
        }
    }

    // Returns the itemId of any item that is near enough to pick up
    private String closeToItem(){
        if (playerLocation != null){
            LatLng playerLatLng = extractLatLngFromLocation(playerLocation);
            for (int i = 0; i < items.size(); i++){
                double distance = SphericalUtil.computeDistanceBetween(items.get(i).getPosition(), playerLatLng);
                if (distance < PICKUP_DISTANCE){
                    return items.get(i).getId();
                }
            }
            return "";
        } else {
            return "";
        }
    }
    private void goToJail(){
        if (playerInTeamZone()){
            gameConnection.setJailStatus(true);
        }
    }
    private void pickUpItem(String itemId){
        for (int i = 0; i < items.size(); i++){
            if (items.get(i).getId().equals(itemId)){
                items.remove(i);
                //TODO update server
                break;
            }
        }
    }

    private void removeItemFromMap(){
        //refresh map to remove items out of list
    }

}
