package com.kludgenics.cgmlogger.app.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.BindingAdapter
import android.support.annotation.UiThread
import android.util.AttributeSet
import android.widget.TextView
import com.kludgenics.cgmlogger.app.EventBus
import com.kludgenics.cgmlogger.app.events.ActivityLifecycleEvent
import com.squareup.otto.Subscribe
import org.ocpsoft.prettytime.PrettyTime
import org.ocpsoft.prettytime.TimeUnit
import org.ocpsoft.prettytime.impl.ResourcesTimeFormat
import org.ocpsoft.prettytime.impl.ResourcesTimeUnit
import org.ocpsoft.prettytime.units.JustNow
import java.util.*
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

/**
 * Created by matthias on 3/24/16.
 */

object FuzzyAgeViewAdapter {
    @BindingAdapter("time") @JvmStatic
    fun setTime(view: FuzzyAgeView, time: Date?) {
        view.time = time
    }
}


@UiThread
class FuzzyAgeView : TextView {
    constructor(context: Context) : super(context)
    constructor(context: Context,
                attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context,
                attrs: AttributeSet? = null,
                defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)
    constructor(context: Context,
                attrs: AttributeSet? = null,
                defStyleAttr: Int = 0,
                defStyleRes: Int = 0) : super(context, attrs, defStyleAttr, defStyleRes)

    var time: Date? by Delegates.observable(null, { metadata: KProperty<*>, from: Date?, to: Date? ->
        onTimeChanged()
    })

    private var attached = false
    private var registered = false

    private val prettyTime: PrettyTime
    private val intentReceiver: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            onTimeChanged()
        }
    }

    private class ThisMinute : ResourcesTimeUnit(), TimeUnit {
        init {
            maxQuantity = 1000L * 60L * 1L
        }

        override fun getResourceKeyPrefix(): String {
            return "JustNow"
        }

    }

    init {
        prettyTime = PrettyTime()
        prettyTime.removeUnit(JustNow::class.java)
        val thisMinute = ThisMinute()
        prettyTime.registerUnit(thisMinute, ResourcesTimeFormat(thisMinute))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!attached) {
            EventBus.instance.register(this)
            attached = true
            registerReciever()
            onTimeChanged()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (attached) {
            EventBus.instance.unregister(this)
            unregisterReceiver()
            attached = false
        }
    }

    @Synchronized
    private fun registerReciever() {
        if (!registered) {
            registered = true
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            context.registerReceiver(intentReceiver, filter, null, handler)
        }
    }

    @Synchronized
    private fun unregisterReceiver() {
        if (registered) {
            registered = false
            context.unregisterReceiver(intentReceiver)
        }
    }

    internal fun onTimeChanged() {
        prettyTime.reference = Date()
        if (attached) {
            text = if (time != null)
                prettyTime.format(time)
            else
                "unspecified"
        }
    }

    @Subscribe
    fun activityLivecycleAvailable (activityLifecycleEvent: ActivityLifecycleEvent) {
        if (activityLifecycleEvent.canonicalName == context.javaClass.canonicalName)
            when (activityLifecycleEvent.event) {
                ActivityLifecycleEvent.ON_PAUSE -> unregisterReceiver()
                ActivityLifecycleEvent.ON_RESUME -> {
                    registerReciever()
                    onTimeChanged()
                }
            }
    }
}
