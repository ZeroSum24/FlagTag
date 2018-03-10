package hangryhippos.cappturetheflag;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.content.Intent;
import android.location.Location;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.nio.charset.Charset;

public class PlayActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        CreateNdefMessageCallback {



    private Button lyricsButton;
    private Button guessButton;
    private RelativeLayout settingsMenu;
    private View congratsOverlay;
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

        mGoogleApiClient = GoogleApiHandler.getInstance(PlayActivity.this).getApiClient();

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

        LatLngBounds ADELAIDE = new LatLngBounds(new LatLng(-35.0, 138.58), new LatLng(-34.9, 138.61));

        // Constrain the camera target to the Adelaide bounds.
        mMap.setLatLngBoundsForCameraTarget(ADELAIDE);

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
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

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
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        Toast.makeText(this, (msg.getRecords()[0].getPayload().toString()), Toast.LENGTH_SHORT).show();
        if (((msg.getRecords()[0].getPayload())).toString()=="Tag, you're it!"){
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

}
