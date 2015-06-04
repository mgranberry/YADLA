package com.kludgenics.cgmlogger.app.service

import android.app.AlarmManager
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.format.DateUtils
import android.util.Log
import com.google.android.gms.gcm
import com.google.android.gms.gcm.*
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.bind.DateTypeAdapter
import com.kludgenics.cgmlogger.app.R
import com.kludgenics.cgmlogger.app.util.DateTimeConverter
import com.kludgenics.cgmlogger.model.glucose.BloodGlucoseRecord
import com.kludgenics.cgmlogger.model.location.places.GooglePlacesApi
import com.kludgenics.cgmlogger.model.nightscout.CalibrationEntry
import com.kludgenics.cgmlogger.model.nightscout.MbgEntry
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiEndpoint
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiEntry
import io.realm.Realm
import org.jetbrains.anko.*
import org.joda.time.DateTime
import org.joda.time.DateTimeComparator
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.converter.GsonConverter
import rx.lang.kotlin.*
import java.util.*
import kotlin.properties.Delegates
import kotlin.Collection.*

/**
 * Created by matthiasgranberry on 5/31/15.
 */

public class TaskService : GcmTaskService(), AnkoLogger {
    companion object {
        public val TASK_SYNC_TREATMENTS: String = "sync_treatments"
        public val TASK_SYNC_ENTRIES_PERIODIC: String = "sync_entries_periodic"
        public val TASK_SYNC_ENTRIES_FULL: String = "sync_entries_full"
        public val TASK_LOOKUP_LOCATIONS: String = "lookup_locations"

        public fun cancelNightscoutTasks(context: Context) {
            Log.i ("TaskService", "Nightscout task cancellation requested by ${context}")
            val networkManager = GcmNetworkManager.getInstance(context)
            networkManager.cancelTask(TASK_SYNC_ENTRIES_FULL, javaClass<TaskService>())
            networkManager.cancelTask(TASK_SYNC_ENTRIES_PERIODIC, javaClass<TaskService>())
            networkManager.cancelTask(TASK_SYNC_TREATMENTS, javaClass<TaskService>())
        }

        public fun scheduleNightscoutPeriodicTasks(context: Context) {
            Log.i ("TaskService", "Periodic Nightscout sync requested by ${context}")
            val networkManager = GcmNetworkManager.getInstance(context)
            networkManager.schedule(scheduleNightscoutTreatmentPeriodicSync())
            networkManager.schedule(scheduleNightscoutEntriesPeriodicSync())
        }

        public fun scheduleNightscoutEntriesFullSync(context: Context) {
            Log.i ("TaskService", "Full sync requested by ${context}")
            val networkManager = GcmNetworkManager.getInstance(context)
            return networkManager.schedule(OneoffTask.Builder().setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setExecutionWindow(0, 30)
                    .setPersisted(true)
                    .setService(javaClass<TaskService>())
                    .setUpdateCurrent(true)
                    .setTag(TASK_SYNC_ENTRIES_FULL)
                    .build())
        }

        public fun scheduleNightscoutEntriesPeriodicSync(): PeriodicTask {
            return PeriodicTask.Builder().setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setPeriod(60 * 30)
                    .setFlex(60 * 10)
                    .setPersisted(true)
                    .setService(javaClass<TaskService>())
                    .setUpdateCurrent(true)
                    .setTag(TASK_SYNC_ENTRIES_PERIODIC)
                    .build()
        }

        public fun scheduleNightscoutTreatmentPeriodicSync(): OneoffTask {
            /*return PeriodicTask.Builder().setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setPeriod(1 * 60)
                    .setFlex(10)
                    .setPersisted(true)
                    .setService(javaClass<TaskService>())
                    .setUpdateCurrent(true)
                    .setTag(TASK_SYNC_TREATMENTS)
                    .build()*/
            return OneoffTask.Builder().setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setExecutionWindow(0, 30)
                    .setPersisted(true)
                    .setService(javaClass<TaskService>())
                    .setUpdateCurrent(true)
                    .setTag(TASK_SYNC_TREATMENTS)
                    .build()
        }
    }

    val prefs: SharedPreferences by Delegates.lazy {
        val p = defaultSharedPreferences
        p.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
        p
    }

    val sharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener {
        sharedPreferences, s ->
        val resources = getResources()
        when (s) {
            resources.getString(R.string.nightscout_uri),
            resources.getString(R.string.nightscout_enable) -> {
                nightscoutEndpoint = createNightscoutService()
            }
        }
    }

    val gsonConverter: GsonConverter by Delegates.lazy {
        GsonConverter(GsonBuilder()
                .registerTypeAdapter(javaClass<DateTime>(), DateTimeConverter())
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .excludeFieldsWithoutExposeAnnotation()
                .create())
    }

    var nightscoutEndpoint: NightscoutApiEndpoint? = null

    override fun onRunTask(taskParams: TaskParams?): Int {

        val adapter = createNightscoutService()
        if (adapter != null)
            nightscoutEndpoint = createNightscoutService()
        else
            cancelNightscoutTasks(ctx)

        return when (taskParams?.getTag()) {
            TASK_LOOKUP_LOCATIONS -> {
                error("Location lookup currently unimplemented")
                GcmNetworkManager.RESULT_FAILURE
            }
            TASK_SYNC_TREATMENTS -> {
                info("Beginning Nightscout treatment sync")
                syncTreatments()
            }
            TASK_SYNC_ENTRIES_FULL -> {
                val count = 50000
                info("Beginning full Nightscout entry sync")
                syncTreatments()
                //syncEntries(count)
            }
            TASK_SYNC_ENTRIES_PERIODIC -> {
                val count = 10
                info("Beginning periodic Nightscout entry sync")
                syncEntries(count)
            }
            else -> {
                error ("Unrecognized tag ${taskParams?.getTag()} in onRunTask")
                GcmNetworkManager.RESULT_FAILURE
            }  // unrecognized tag, should never happen
        }
    }

    private fun syncEntries(count: Int): Int {
        val entries = nightscoutEndpoint?.getEntries(count)
        val realm: Realm = Realm.getInstance(ctx)
        realm.use {
            try {
                realm.beginTransaction()
                if (entries != null) {
                    entries.forEach {
                        val ro = it.asRealmObject()
                        when (ro) {
                            is BloodGlucoseRecord -> {
                                realm.copyToRealmOrUpdate(ro)
                            }
                        }
                    }
                    info("Entry sync completed")
                    realm.commitTransaction()
                    return GcmNetworkManager.RESULT_SUCCESS
                } else {
                    info("Entry sync failed")
                    realm.cancelTransaction()
                    return GcmNetworkManager.RESULT_RESCHEDULE
                }
            } catch (t: RuntimeException) {
                info("Entry sync failed: ${t}")
                realm.cancelTransaction()
                return GcmNetworkManager.RESULT_FAILURE
            }
        }
    }



    private fun syncTreatments(): Int {
        val treatments = nightscoutEndpoint?.getTreatments()
        val realm: Realm = Realm.getInstance(ctx)
        realm.use {
            try {
                realm.beginTransaction()
                if (treatments != null) {
                    treatments.forEach {
                        realm.copyToRealmOrUpdate(it.toTreatment())
                    }
                    info("Treatment ync completed")
                    realm.commitTransaction()
                    return GcmNetworkManager.RESULT_SUCCESS
                } else {
                    info("Treatment sync failed")
                    realm.cancelTransaction()
                    return GcmNetworkManager.RESULT_RESCHEDULE
                }
            } catch (t: RuntimeException) {
                info("Treatment sync failed: ${t}")
                realm.cancelTransaction()
                return GcmNetworkManager.RESULT_FAILURE
            }
        }
    }

    private fun createNightscoutService(): NightscoutApiEndpoint? {
        val uri = prefs.getString(getResources().getString(R.string.nightscout_uri), "")
        val enabled = prefs.getBoolean(getResources().getString(R.string.nightscout_enable), false)
        if (enabled && !uri.isNullOrBlank())
            return RestAdapter.Builder()
                .setEndpoint(uri)
                .setConverter(gsonConverter)
                .build().create(javaClass<NightscoutApiEndpoint>())
        else
            return null
    }

}

