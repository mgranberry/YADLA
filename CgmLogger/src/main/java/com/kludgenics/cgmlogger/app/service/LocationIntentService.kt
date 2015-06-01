package com.kludgenics.cgmlogger.app.service

import android.app.IntentService
import android.app.PendingIntent
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.text.format.DateUtils
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.kludgenics.cgmlogger.data.activity.PlayServicesActivity
import com.kludgenics.cgmlogger.data.location.data.Position
import com.kludgenics.cgmlogger.extension.Geofence
import com.kludgenics.cgmlogger.extension.GeofencingRequest
import org.jetbrains.anko.*
import com.kludgenics.cgmlogger.extension.*
import com.kludgenics.cgmlogger.util.EventBus
import io.realm.Realm
import java.util.Date

/**
 * Created by matthiasgranberry on 5/29/15.
 */
public class LocationIntentService : IntentService("location"), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>, AnkoLogger {
    var client: GoogleApiClient? = null

    companion object {
        val REQUEST_LOCATION_UPDATE = 1

        val ACTION_LOCATION_UPDATE = "com.kludgenics.action.LOCATION_UPDATE"
        val ACTION_ACTIVITY_UPDATE = "com.kludgenics.action.ACTIVITY_UPDATE"

        val ACTION_BOOT = "com.kludgenics.action.BOOT"
        val ACTION_START_LOCATION_UPDATES = "com.kludgenics.action.START_LOCATION_UPDATES"

        private val ACTIVITY_DETECTION_INTERVAL_IN_MILLISECONDS: Long = 1200000
        private val LOCATION_MAX_INTERVAL_MILLISECONDS: Long = DateUtils.MINUTE_IN_MILLIS * 30
        private val LOCATION_FASTEST_INTERVAL_MILLISECONDS: Long = DateUtils.MINUTE_IN_MILLIS
    }

    override fun onCreate() {
        super<IntentService>.onCreate()
        client = GoogleApiClient.Builder(ctx)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build()
    }

    override fun onConnectionSuspended(reason: Int) {
       info("onConnectionSuspended($reason)")
    }

    override fun onConnected(p0: Bundle?) {
        info("onConnected($p0)")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        EventBus.get().onNext(p0)
    }

    override fun onResult(status: Status) {
        EventBus.get().onNext(status)
    }

    override fun onHandleIntent(intent: Intent?) {
        if ((client != null) && !client!!.isConnected())
            client!!.blockingConnect()

        when (intent?.getAction()) {
            ACTION_LOCATION_UPDATE -> {
                if (LocationResult.hasResult(intent)) {
                    logLocations(LocationResult.extractResult(intent).getLocations())
                }
            }
            ACTION_ACTIVITY_UPDATE -> {
                if (ActivityRecognitionResult.hasResult(intent)) {
                    val result = ActivityRecognitionResult.extractResult(intent)
                    val likelyActivity = result.getMostProbableActivity()
                    info(likelyActivity.toString())
                    val realm: Realm = Realm.getInstance(ctx)
                    realm.use {
                        realm.create<PlayServicesActivity> {
                            this.setActivityId(likelyActivity.getType())
                            this.setConfidence(likelyActivity.getConfidence())
                            this.setTime(Date())
                        }
                    }
                }
            }
            ACTION_BOOT, ACTION_START_LOCATION_UPDATES -> {
                requestLocationUpdates()
                requestActivityUpdates()
            }
            else -> info("received unhandled intent")
        }
    }

    private fun logLocations(locations: List<Location>) {
        val realm: Realm = Realm.getInstance(ctx)
        realm.use {
            for (location in locations) {
                info("Received location: ${location}")

                realm.create<Position> {
                    this.setTime(Date(System.currentTimeMillis() - (SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos())/1000000))
                    this.setAccuracy(location.getAccuracy())
                    this.setLatitude(location.getLatitude())
                    this.setLongitude(location.getLongitude())
                }
            }
        }

    }

    private fun createPendingIntent(action: String): PendingIntent {
        return PendingIntent.getService(ctx, REQUEST_LOCATION_UPDATE,
                intentFor<LocationIntentService>()
                        .setAction(action),
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun requestActivityUpdates() {
        info("Requesting activity updates")
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                client,
                ACTIVITY_DETECTION_INTERVAL_IN_MILLISECONDS,
                createPendingIntent(ACTION_ACTIVITY_UPDATE)
        ).setResultCallback(this);
    }

    private fun requestLocationUpdates() {
        info("Starting location updates")
        LocationServices.FusedLocationApi
                .requestLocationUpdates(client,
                        createLocationRequest(),
                        createPendingIntent(ACTION_LOCATION_UPDATE))
    }

    private fun createLocationRequest(): LocationRequest {
        val lastLocation = LocationServices.FusedLocationApi.getLastLocation(client)
        val locationBubble = if (lastLocation != null && lastLocation.getAccuracy() < 50.0)
            lastLocation.getAccuracy()
        else
            50f

        return LocationRequest.create()
                .setExpirationTime(SystemClock.elapsedRealtime() + DateUtils.DAY_IN_MILLIS)
                //.setInterval(DateUtils.MINUTE_IN_MILLIS)
                .setMaxWaitTime(LOCATION_MAX_INTERVAL_MILLISECONDS)
                .setFastestInterval(LOCATION_FASTEST_INTERVAL_MILLISECONDS)
                .setSmallestDisplacement(locationBubble)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
    }

}
