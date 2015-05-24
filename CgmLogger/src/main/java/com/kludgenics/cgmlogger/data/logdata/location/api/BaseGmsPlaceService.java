package com.kludgenics.cgmlogger.data.logdata.location.api;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.*;
import com.kludgenics.cgmlogger.data.logdata.location.data.GooglePlacesLocation;
import com.kludgenics.cgmlogger.data.logdata.location.data.Location;
import com.kludgenics.cgmlogger.data.logdata.location.data.Position;
import rx.Observable;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by matthiasgranberry on 5/17/15.
 */
public class BaseGmsPlaceService extends Service implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    //@Inject
    GoogleApiClient mGoogleApiClient;

    private final IBinder mBinder = new LocalBinder();
    private static final String TAG="BaseGmsPlaceService";

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
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

    public class LocalBinder extends Binder {
        public GeoApi getService() {
            return new GooglePlacesApi(mGoogleApiClient);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        connectGoogleClient();
        return mBinder;
    }

    private void connectGoogleClient() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            Log.i(TAG, "connecting google client");
        }
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
