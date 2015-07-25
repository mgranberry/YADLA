package com.kludgenics.cgmlogger.app.service

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.ArrayMap
import android.util.Log
import com.google.android.gms.gcm.*
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.kludgenics.cgmlogger.app.R
import com.kludgenics.cgmlogger.app.util.DateTimeSerializer
import com.kludgenics.cgmlogger.model.glucose.BloodGlucoseRecord
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiEndpoint
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiEntry
import com.kludgenics.cgmlogger.model.nightscout.NightscoutApiTreatment
import io.realm.Realm
import org.jetbrains.anko.*
import org.joda.time.DateTime
import retrofit.RestAdapter
import retrofit.converter.GsonConverter
import kotlin.properties.Delegates

/**
 * Created by matthiasgranberry on 5/31/15.
 */

public class TaskService : GcmTaskService(), AnkoLogger {
    companion object {
        public val ACTION_SYNC_NOW: String = "com.kludgenics.cgmlogger.action.SYNC"
        public val ACTION_SYNC_FULL: String = "com.kludgenics.cgmlogger.action.SYNC_FULL"
        public val TASK_SYNC_TREATMENTS: String = "sync_treatments"
        public val TASK_SYNC_ENTRIES_PERIODIC: String = "sync_entries_periodic"
        public val TASK_SYNC_ENTRIES_FULL: String = "sync_entries_full"
        public val TASK_LOOKUP_LOCATIONS: String = "lookup_locations"

        private val TREATMENT_SYNC_PERIOD: Long = 60 * 30
        private val TREATMENT_SYNC_FLEX: Long = 60 * 10
        private val ENTRY_SYNC_PERIOD: Long = 60 * 30
        private val ENTRY_SYNC_FLEX: Long = 60 * 10

        public fun syncNow(context: Context) {
            val intent = Intent()
            with(intent) {
                setAction(ACTION_SYNC_NOW)
                setClass(context, javaClass<TaskService>())
            }
            context.startService(intent)
        }

        public fun fullSyncNow(context: Context) {
            val intent = Intent()
            with(intent) {
                setAction(ACTION_SYNC_FULL)
                setClass(context, javaClass<TaskService>())
            }
            context.startService(intent)
        }

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
            networkManager.schedule(createPeriodicTreatmentTask())
            networkManager.schedule(createPeriodicEntryTask())
        }

        public fun scheduleNightscoutEntriesFullSync(context: Context) {
            Log.i ("TaskService", "Full sync requested by ${context}")
            val networkManager = GcmNetworkManager.getInstance(context)
            return networkManager.schedule(OneoffTask.Builder()
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setExecutionWindow(0, 30)
                    .setPersisted(true)
                    .setService(javaClass<TaskService>())
                    .setUpdateCurrent(true)
                    .setTag(TASK_SYNC_ENTRIES_FULL)
                    .build())
        }

        public fun createPeriodicEntryTask(): PeriodicTask {
            return PeriodicTask.Builder().setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setPeriod(ENTRY_SYNC_PERIOD)
                    .setFlex(ENTRY_SYNC_FLEX)
                    .setPersisted(true)
                    .setService(javaClass<TaskService>())
                    .setUpdateCurrent(true)
                    .setTag(TASK_SYNC_ENTRIES_PERIODIC)
                    .build()
        }

        public fun createPeriodicTreatmentTask(): PeriodicTask {
            return PeriodicTask.Builder().setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setPeriod(TREATMENT_SYNC_PERIOD)
                    .setFlex(TREATMENT_SYNC_FLEX)
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
                createNightscoutTasks()
            }
        }
    }

    val gsonConverter: GsonConverter by Delegates.lazy {
        GsonConverter(GsonBuilder()
                .registerTypeAdapter(javaClass<Int>(), IntegerTypeAdapter() )
                .registerTypeAdapter(javaClass<DateTime>(), DateTimeSerializer())
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .excludeFieldsWithoutExposeAnnotation()
                .create())
    }

    val tasks: MutableMap<String, NightscoutTask> = ArrayMap()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.getAction()) {
            ACTION_SYNC_NOW -> {
                async {
                    onRunTask(TaskParams(TASK_SYNC_ENTRIES_PERIODIC))
                    //onRunTask(TaskParams(TASK_SYNC_TREATMENTS))
                }
                2
            }
            ACTION_SYNC_FULL -> {
                async {
                    onRunTask(TaskParams(TASK_SYNC_ENTRIES_FULL))
                    //onRunTask(TaskParams(TASK_SYNC_TREATMENTS))
                }
                2
            }
            else -> super<GcmTaskService>.onStartCommand(intent, flags, startId)
        }
    }

    override fun onInitializeTasks() {
        super<GcmTaskService>.onInitializeTasks()
        info("initializing tasks")
        val resources = getResources()
        if (prefs.getBoolean(resources.getString(R.string.nightscout_enable), false)  &&
                !(prefs.getString(resources.getString(R.string.nightscout_uri), "").isBlank())) {
            scheduleNightscoutPeriodicTasks(this)
        } else
            cancelNightscoutTasks(this)
    }

    override fun onRunTask(taskParams: TaskParams): Int {
        if (tasks.isEmpty())
            createNightscoutTasks()

        return when (taskParams.getTag()) {
            TASK_LOOKUP_LOCATIONS -> {
                error("Location lookup currently unimplemented")
                GcmNetworkManager.RESULT_FAILURE
            }
            in tasks.keySet() -> {
                // if the tasks were reset mid-stream, don't crash
                info ("Starting ${taskParams.getTag()}")
                val r = tasks.get(taskParams.getTag())?.call() ?: GcmNetworkManager.RESULT_RESCHEDULE
                info ("Finished ${taskParams.getTag()}")
                r
            }
            else -> {
                error ("Unrecognized tag ${taskParams.getTag()} in onRunTask")
                GcmNetworkManager.RESULT_FAILURE
            }  // unrecognized tag, should never happen
        }
    }

    private fun createNightscoutTasks(): Unit {
        val uri = prefs.getString(getResources().getString(R.string.nightscout_uri), "")
        val enabled = prefs.getBoolean(getResources().getString(R.string.nightscout_enable), false)
        val endpoint = if (enabled && !uri.isNullOrBlank())
            RestAdapter.Builder()
                    .setEndpoint(uri)
                    .setConverter(gsonConverter)
                    .build().create(javaClass<NightscoutApiEndpoint>())
            else
                null
        info ("closing tasks in createNightscoutTasks")
        tasks.clear()
        if (endpoint != null) {
            tasks.put(TASK_SYNC_TREATMENTS, NightscoutTreatmentTask(ctx, endpoint))
            tasks.put(TASK_SYNC_ENTRIES_FULL, NightscoutEntryTask(ctx, endpoint, 50000))
            tasks.put(TASK_SYNC_ENTRIES_PERIODIC, NightscoutEntryTask(ctx, endpoint))
        } else
            cancelNightscoutTasks(ctx)
    }

    override fun onDestroy() {
        info ("onDestroy()")
        tasks.clear()
        super<GcmTaskService>.onDestroy()
    }

    public class IntegerTypeAdapter: TypeAdapter<Int>() {

        override fun write(writer: JsonWriter, value: Int?) {
            if (value == null) {
                writer.nullValue();
                return;
            }
            writer.value(value);
        }

        override fun read(reader: JsonReader): Int? {
            reader.peek()
            return when (reader.peek()) {
                JsonToken.NUMBER -> {
                    val value = reader.nextDouble()
                    value.toInt()
                }
                else -> null
            }
        }

    }
}

