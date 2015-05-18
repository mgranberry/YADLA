package com.kludgenics.logdata.location.api;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.kludgenics.logdata.location.data.Location;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by matthiasgranberry on 5/17/15.
 */
public class BaseGmsPlaceService extends Service implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    //@Inject
    GoogleApiClient mGoogleApiClient;
    List<Location> mLocationQueue;

    @Override
    public void onCreate() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi( Places.GEO_DATA_API )
                .addApi( Places.PLACE_DETECTION_API )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connectGoogleClient();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectGoogleClient();
    }

    private void disconnectGoogleClient() {
        if ( mGoogleApiClient != null && (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) ) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public IBinder onBind(Intent intent) {
        connectGoogleClient();
        return null;
    }

    private void connectGoogleClient() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
