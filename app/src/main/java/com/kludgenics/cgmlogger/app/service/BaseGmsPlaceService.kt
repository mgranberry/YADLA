package com.kludgenics.cgmlogger.app.service

import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
//import com.kludgenics.cgmlogger.model.location.places.GooglePlacesApi
import com.kludgenics.cgmlogger.model.location.GeoApi

/**
 * Created by matthiasgranberry on 5/17/15.
 */
public class BaseGmsPlaceService : Service(), GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    var mGoogleApiClient: GoogleApiClient? = null

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        connectGoogleClient()
        return super<Service>.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectGoogleClient()
    }

    private fun disconnectGoogleClient() {
        if ((mGoogleApiClient!!.isConnected() || mGoogleApiClient!!.isConnecting())) {
            mGoogleApiClient!!.disconnect()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    public inner class LocalBinder(private val client: GoogleApiClient) : Binder() {
        public fun getService(): GeoApi? {
            return null //GooglePlacesApi(client)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Log.i(TAG, "onBind")
        connectGoogleClient()
        return LocalBinder(mGoogleApiClient!!)
    }

    private fun connectGoogleClient() {
        if (mGoogleApiClient != null) {
            Log.i(TAG, "connecting google client")
            mGoogleApiClient!!.connect()
            Log.i(TAG, "connected")
        }
    }

    override fun onConnected(bundle: Bundle?) {
        Log.d(TAG, "connected")
    }

    override fun onConnectionSuspended(i: Int) {
        Log.d(TAG, "connection suspended")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult?) {
        Log.d(TAG, "connection failed")
    }

    companion object {
        private val TAG = "BaseGmsPlaceService"
    }


}