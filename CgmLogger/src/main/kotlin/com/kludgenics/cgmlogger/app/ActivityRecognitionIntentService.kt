package com.kludgenics.cgmlogger.app

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import org.jetbrains.anko.*
import com.kludgenics.cgmlogger.data.logdata.glucose.data.*

public class ActivityRecognitionIntentService : IntentService("ActivityRecognitionIntentService") {

    override fun onHandleIntent(intent: Intent) {
        async{
            Log.d("Async", "running async")
        }
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            val detectedActivity = result.getMostProbableActivity()

            val confidence = detectedActivity.getConfidence()
            val mostProbableName = getActivityName(detectedActivity.getType())

            val i = Intent("SAVVY")
            i.putExtra("act", mostProbableName)
            i.putExtra("confidence", confidence)

            Log.d("Saquib", "mostProbableName " + mostProbableName)
            Log.d("Saquib", "Confidence : " + confidence)

            //Send Broadcast
            this.sendBroadcast(i)

        }
    }
    public fun handleBg(bg: BloodGlucose) {
        bg.asMmol()
    }
    private fun getActivityName(type: Int): String {
        when (type) {
            DetectedActivity.IN_VEHICLE -> return "in_vehicle"
            DetectedActivity.ON_BICYCLE -> return "on_bicycle"
            DetectedActivity.WALKING -> return "walking"
            DetectedActivity.STILL -> return "still"
            DetectedActivity.TILTING -> return "tilting"
            DetectedActivity.UNKNOWN -> return "unknown"
            DetectedActivity.RUNNING -> return "running"
        }
        return "n/a"
    }
}