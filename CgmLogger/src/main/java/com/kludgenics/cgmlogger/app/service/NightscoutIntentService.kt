package com.kludgenics.cgmlogger.app.service

import android.app.AlarmManager
import android.app.IntentService
import android.content.Intent
import android.text.format.DateUtils
import com.google.android.gms.gcm.*
import org.jetbrains.anko.*

/**
 * Created by matthiasgranberry on 5/31/15.
 */

public class NightscoutIntentService: GcmTaskService() {
    companion object {
        public val TASK_SYNC_TREATMENTS: String = "sync_treatments"
        public val TASK_SYNC_ENTRIES_PERIODIC: String = "sync_entries_periodic"
        public val TASK_SYNC_ENTRIES_FULL: String = "sync_entries_full"
        public val TASK_LOOKUP_LOCATIONS: String = "lookup_locations"
        public fun scheduleNightscoutPeriodicSync(): PeriodicTask {
            return PeriodicTask.Builder().setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setPeriod(60 * 60)
                    .setFlex(60 * 30)
                    .setPersisted(true)
                    .setService(NightscoutIntentService::class)
                    .setUpdateCurrent(true)
                    .setTag(TASK_SYNC_ENTRIES_PERIODIC)
                    .build()
        }
    }
    override fun onRunTask(taskParams: TaskParams?): Int {
        return when (taskParams?.getTag()) {
            TASK_LOOKUP_LOCATIONS -> {
                GcmNetworkManager.RESULT_SUCCESS
            }
            TASK_SYNC_TREATMENTS -> {
                GcmNetworkManager.RESULT_SUCCESS
            }
            TASK_SYNC_ENTRIES_FULL -> {
                GcmNetworkManager.RESULT_SUCCESS
            }
            TASK_SYNC_ENTRIES_PERIODIC -> {
                GcmNetworkManager.RESULT_SUCCESS
            }

            else -> GcmNetworkManager.RESULT_FAILURE
        }
    }
}

