package com.kludgenics.cgmlogger.app.events

import android.content.Context
import com.kludgenics.cgmlogger.app.EventBus

/**
 * Created by matthias on 3/24/16.
 */


data class ActivityLifecycleEvent(val canonicalName: String, val event: Int) {
    companion object {
        const val ON_PAUSE = 0
        const val ON_RESUME = 1
    }
}

fun Context.postOnPause() {
    EventBus.instance.post(ActivityLifecycleEvent(this.javaClass.canonicalName, ActivityLifecycleEvent.ON_PAUSE))
}

fun Context.postOnResume() {
    EventBus.instance.post(ActivityLifecycleEvent(this.javaClass.canonicalName, ActivityLifecycleEvent.ON_RESUME))
}