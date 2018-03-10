package hangryhippos.cappturetheflag;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;


public class MapsFragment extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = false;
    private Location mLastLocation;
    private static final String TAG = "MapsActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_map);

        //Initialises Difficulty and Points Systems

        Log.e("MapsActivityCreate", "DifficultyLevel level: ");
        generalActivityIntents();
        displayPointsSystem();

        //Obtain the SupportMapFragment
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);


        //Get notified when the map is ready to be used. Long-running activities are performed asynchronously
        //in order to keep the user interface responsive
        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
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


        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        //Should be the code which enables markers to be displayed on the map

//        for (int i = 0; i < mapSize; i++) {
//            Placemark currentPlacemark = mapCurrent.getPlacemark(i);
//            if(!currentPlacemark.isPlacemarkRemoved()) {
//                Double xCoordinate = currentPlacemark.getPoint().getLat();
//                Double yCoordinate = currentPlacemark.getPoint().getLong();
//                String description = getString(currentPlacemark.getDescription());
//                int markerIcon = currentPlacemark.returnMarkerImage();
//
//                LatLng coordinates = new LatLng(xCoordinate, yCoordinate);
//                builder.include(coordinates);
//
//                Marker currentMarker = mMap.addMarker(new MarkerOptions().position(coordinates).title(description)
//                        .icon(BitmapDescriptorFactory.fromResource(markerIcon)));
//                markerList.add(currentMarker);
//                Log.e("MapLoad", "Marker added: " + true);
//            } else {
//                markerList.add(null);
//            }
//        }

        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
        googleMap.moveCamera(cu);

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
//        displayPointsSystem();
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

        //Do something with the marker here

//        for (int i = 0; i < markerList.size(); i++) {
//            Marker currentMarker = markerList.get(i);
//
//            if (currentMarker != null) {
//                LatLng point = currentMarker.getPosition();
//
//                target.setLatitude(point.latitude);
//                target.setLongitude(point.longitude);
//                if (current.distanceTo(target) < 8) { //Adjust distance to allow word pickup
//                    // bingo!
//
//                    mapOverlayCall(i); //calls the congrats map overlay
//                    currentMarker.setVisible(false); //Makes the marker invisible
//                    currentMarker.remove(); //remove marker from map
//                    markerList.set(i, null); //Should remove marker from list without reducing the size of the list
//                    accessGame.getCurrentSong().getMap().getPlacemark(i).setPlacemarkRemoved(true);
//                    displayPointsSystem(); //updates the points system with new points
//
//                }
//            }
//        }
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
    public void onConnectionSuspended(int flag) {
        System.out.println(" >>>> onConnectionSuspended");
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

    /**
     * Method checks the points which the user has collected and displays them on the activity.
     */
    private void displayPointsSystem() {
        //POINTS SYSTEM

//        //Initialises the Views here
//        TextView setCurrentPoints = findViewById(R.id.currentPoints);
//        TextView setTotalPoints = findViewById(R.id.totalPoints);
//
//        //Accesses the data needed to display the views
//        String currentPoints = Integer.toString(accessGame.getPointsSystem().getCurrentPoints());
//        String totalPoints = Integer.toString(accessGame.getPointsSystem().getTotalPoints());
//
//        //Sets the views here
//        setCurrentPoints.setText(currentPoints);
//        setTotalPoints.setText(totalPoints);

    }

    /**
     * Method manages the two intent buttons (lyrics and guess) which
     * governs which activity is accessed (button name respective). It also governs the settings
     * button.
     * <p>
     * It also highlights the chosen button to show the user which has been clicked.
     */
    private void generalActivityIntents() {
        //GENERAL ACTIVITY FUNCTIONS

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

    /**
     * Overrides method to prevent the user accessing the last activity and allowing them the
     * option to quit the session. It also updates the users score to be submitted by the
     * Main Menu Activity.
     */
    @Override
    public void onBackPressed() {
//        AlertDialogManager alert = new AlertDialogManager();
//        alert.showAlertDialog(MapsActivity.this, null,
//                getString(R.string.cQuitAlertHeader), getString(R.string.cQuitAlertButton),
//                true, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        int pointsToSubmit = accessGame.getPointsSystem().pointsToSubmit();
//                        accessGame.setUsersScoreToSubmit(pointsToSubmit);
//
//                        startActivity(new Intent(MapsActivity.this, MainMenuActivity.class));
//                    }
//                });
    }

    /**
     * Overrides method to close the settings menu if anywhere outside the settings menu is touched
     * after it is opened. It also prevents other buttons being access whilst the collected word
     * overlay is displayed.
     */
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {

//        //Close on touch outside for settings
//        Rect settingsRect = new Rect();
//        settingsMenu.getGlobalVisibleRect(settingsRect);
//        boolean isClickInSettingsMenu = !settingsRect.contains((int) ev.getRawX(), (int) ev.getRawY());
//        if (isClickInSettingsMenu && settingsMenu.getVisibility() == View.VISIBLE) {
//            settingsMenu.setVisibility(View.INVISIBLE);
//            return true;
//        }
//
//        //Don't touch outside for Map Congrats Overlay
//        Rect congratsRect = new Rect();
//        congratsOverlay.getGlobalVisibleRect(congratsRect);
//        boolean isClickOutsideOverlay = !congratsRect.contains((int) ev.getRawX(), (int) ev.getRawY());
//        if (isClickOutsideOverlay && congratsOverlay.getVisibility() == View.VISIBLE) {
//            return true;
//        }
//
//        return super.dispatchTouchEvent(ev);
//    }
}
