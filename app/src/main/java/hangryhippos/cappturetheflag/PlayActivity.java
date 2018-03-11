package hangryhippos.cappturetheflag;

import android.Manifest;
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
import android.provider.Settings;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.List;
import java.nio.charset.Charset;
import java.util.Timer;

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
            new LatLngBounds(new LatLng(55.940511, -3.195196), new LatLng(55.942117, -3.186929));

    // area where team 1 respawns
    private LatLngBounds respawnZoneTeam1;
    private LatLngBounds respawnZoneTeam2;
    // team 1's area in general (covers respawn area too to make tagging easier)
    private LatLngBounds zoneTeam1;
    private LatLngBounds zoneTeam2;
    private LatLngBounds neutralZone;
    // Gives the respawn area for the player's team
    // NB different to the respawn area for two teams as it changes depending on your team
    private LatLngBounds respawnArea;
    // Gives the last location of the player
    private Location playerLocation;
    // Indicates if the player has been tagged by an enemy
    private boolean playerTagged;
    // Indicates if the player is in their respawn area
    private boolean playerInRespawnArea;
    // Shows the progress of the countdown
    private ProgressBar countdownProgress;
    // Length of time (in milliseconds that players have to wait to get released
    private int waitingTime = 20000;
    private CountDownTimer countDownTimer;
    private enum TimerStatus {
        STARTED,
        STOPPED
    }
    private TimerStatus timerStatus = TimerStatus.STOPPED;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        //Initialises Difficulty and Points Systems
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
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

        // Constrain the camera target to the Adelaide bounds.
        mMap.setLatLngBoundsForCameraTarget(EDINBURGH_MEADOWS);

        // Set the camera to the greatest possible zoom level that includes the
// bounds
//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(ADELAIDE, 0));


        try {
            // Visualise current position with a small blue circle
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException se) {
            System.out.println("Security exception thrown [onMapReady]");
        }
        // Add ‘‘My location’’ button to the user interface
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
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

        Log.d("New Location", "Lat: " + current.getLatitude() + "Lng : " + current.getLongitude());
        Location target = new Location("target");

        // If the player was tagged, check they are still in the area
        if (playerTagged){
            // If they aren't (or are on their way) reset the timer
            if (!playerInRespawnArea(respawnArea)){
                stopCountDownTimer();
            } else if (timerStatus == TimerStatus.STOPPED){
                startCountDownTimer();
            }
        }

    }


    @Override
    public void onConnectionSuspended(int flag) {
        System.out.println(" >>>> onConnectionSuspended");
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String text = ("Tag, you're it!");
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMimeRecord(
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
    }

    private void refresh(){
        // TODO
        // send location to database
        // if the player has been tagged, they must head back to respawn (can't see anything else)
        //  - if the player needed to respawn and has entered the area, check that they are still there
        //  - if they need to respawn and have moved away from the respawn, add time to the respawning and warn the player
        //  - if they need to respawn but haven't entered the respawn area, keep disabled, but add no time penalty
        //
        // if player was not tagged then continue with the rest of the logic here:
        // check if enemy flag revealed/hidden (store location on player's phone but keep secret)
        // show enemy flag if revealed
        // check if you're close to the flag
        // reveal flag if the player is nearby
        // if the flag was already revealed, and the player has waited 5 seconds nearby, collect the flag
        // if the flag has been collected by either team, notify all users of the player(s) with the flag and where they are
        // check if you're close to an item
        // if you are, pick it up (and remove the item from the map)
        //
        // add logic for items as well? Leave for now
    }

    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        Toast.makeText(this, (msg.getRecords()[0].getPayload().toString()), Toast.LENGTH_SHORT).show();
        if (((msg.getRecords()[0].getPayload())).toString().equals("Tag, you're it!")){
            System.out.println("Tagged!");
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
    private boolean playerInRespawnArea(LatLngBounds respawnArea){
        if(playerLocation != null){
            LatLng playerLatLng = extractLatLngFromLocation(playerLocation);
            return respawnArea.contains(playerLatLng);
        } else {
            return false;
        }
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



        respawnZoneTeam1 = new LatLngBounds(new LatLng(x1, y1), new LatLng(x2, y2));
        zoneTeam1 = new LatLngBounds(new LatLng(x1, y1), new LatLng(x3, y2));
        neutralZone = new LatLngBounds(new LatLng(x3, y1), new LatLng(x4, y2));
        zoneTeam2 = new LatLngBounds(new LatLng(x4, y1), new LatLng(x5, y2));
        respawnZoneTeam2 = new LatLngBounds(new LatLng(x5, y1), new LatLng(x6, y2));

    }

    //TODO would be better if didn't show enemy respawn zone (unnecessary)
    //Adds zones to the map and displays them. Called only when loading map for first time in the activity
    private void addZones(){
        // Check that the zones have been calculated, if not then stop
        if (respawnZoneTeam1 == null || zoneTeam1 == null
                || neutralZone == null || zoneTeam2 == null || respawnZoneTeam2 == null
                ) return;
        // Remove anything that might have been on the map before
        mMap.clear();
        Polygon rectRespawnTeam1 = mMap.addPolygon(calculateRectangle(respawnZoneTeam1));
        rectRespawnTeam1.setTag(getString(R.string.respawn_1));
        rectRespawnTeam1.setZIndex(1);
        rectRespawnTeam1.setFillColor(0x7F2196F3);
        rectRespawnTeam1.setStrokeWidth(0);

        Polygon rectTeam1 = mMap.addPolygon(calculateRectangle(zoneTeam1));
        rectTeam1.setTag(getString(R.string.zone_1));
        rectTeam1.setFillColor(0x7F1565C0);
        rectTeam1.setStrokeWidth(0);

        Polygon rectNeutral = mMap.addPolygon(calculateRectangle(neutralZone));
        rectNeutral.setTag(getString(R.string.zone_neutral));
        rectNeutral.setFillColor(0x7F9E9E9E);
        rectNeutral.setStrokeWidth(0);

        Polygon rectTeam2 = mMap.addPolygon(calculateRectangle(zoneTeam2));
        rectTeam2.setTag(getString(R.string.zone_2));
        rectTeam2.setFillColor(0x7FC62828);
        rectTeam2.setStrokeWidth(0);

        Polygon rectRespawnTeam2 = mMap.addPolygon(calculateRectangle(respawnZoneTeam2));
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



}
