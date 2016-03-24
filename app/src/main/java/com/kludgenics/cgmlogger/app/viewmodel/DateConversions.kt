package com.kludgenics.cgmlogger.app.viewmodel

import android.databinding.BindingConversion

import org.joda.time.LocalDateTime
import org.ocpsoft.prettytime.PrettyTime
import org.ocpsoft.prettytime.TimeUnit
import org.ocpsoft.prettytime.impl.ResourcesTimeFormat
import org.ocpsoft.prettytime.impl.ResourcesTimeUnit

import java.util.Date

/**
 * Created by matthias on 3/21/16.
 */
object DateConversions {
    private class JustNow : ResourcesTimeUnit(), TimeUnit {

        init {
            maxQuantity = 1000L * 60L * 1L
        }

        override fun getResourceKeyPrefix(): String {
            return "JustNow"
        }

    }

    private val _prettyTime = PrettyTime()

    private val prettyTime: PrettyTime get() {
        _prettyTime.reference = Date()
        return _prettyTime
    }

    init {
        _prettyTime.removeUnit(org.ocpsoft.prettytime.units.JustNow::class.java)
        val justNow = JustNow()
        _prettyTime.registerUnit(justNow, ResourcesTimeFormat(justNow))
    }

    @JvmStatic
    @BindingConversion
    fun convertDateToString(date: Date): CharSequence {
        return prettyTime.formatUnrounded(date)
    }

    @JvmStatic
    @BindingConversion
    fun convertLocalDateTimeToString(localDateTime: LocalDateTime): CharSequence {
        return prettyTime.format(localDateTime.toDate())
    }
}
