package hangryhippos.cappturetheflag;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class GoogleApiHandler implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static GoogleApiHandler instance = null;
    private Context context;
    private ArrayList<GoogleApiClient.ConnectionCallbacks> callbacks;
    private ArrayList<GoogleApiClient.OnConnectionFailedListener> failedList;
    private GoogleApiClient mGoogleApiClient;

    protected GoogleApiHandler(Context context) {
        this.context = context = context;
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).addApi(Games.API)
                    .addScope(Games.SCOPE_GAMES)
                    .build();

        }
        callbacks = new ArrayList<>();
        failedList = new ArrayList<>();
    }

    public static GoogleApiHandler getInstance(Context appcontext) {
        if (instance == null) {
            instance = new GoogleApiHandler(appcontext);
        }
        return instance;
    }

    public void addConnectionCallbacks(GoogleApiClient.ConnectionCallbacks conCallbacks) {
        callbacks.add(conCallbacks);
    }

    public void addOnConnectionFailedListener(GoogleApiClient.OnConnectionFailedListener failedListener) {
        failedList.add(failedListener);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        for (GoogleApiClient.ConnectionCallbacks c : callbacks) {
            c.onConnected(bundle);
        }
    }

    public GoogleApiClient getApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public void onConnectionSuspended(int i) {
        for (GoogleApiClient.ConnectionCallbacks c : callbacks) {
            c.onConnectionSuspended(i);
        }
    }

    public boolean connected() {
        return mGoogleApiClient.isConnected();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        for (GoogleApiClient.OnConnectionFailedListener f : failedList) {
            f.onConnectionFailed(connectionResult);
        }
    }
}