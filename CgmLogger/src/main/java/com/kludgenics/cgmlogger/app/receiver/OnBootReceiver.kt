package com.kludgenics.cgmlogger.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kludgenics.cgmlogger.app.service.LocationIntentService
import com.kludgenics.cgmlogger.app.service.TaskService
import org.jetbrains.anko.*

/**
 * Created by matthiasgranberry on 5/29/15.
 */
public class OnBootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.startService(context.intentFor<LocationIntentService>().setAction(LocationIntentService.ACTION_BOOT))
        context.startService(context.intentFor<TaskService>().setAction(LocationIntentService.ACTION_BOOT))
    }
}
