package com.kludgenics.cgmlogger.app.viewmodel

import android.databinding.Bindable
import android.databinding.PropertyChangeRegistry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.kludgenics.cgmlogger.app.BR
import com.kludgenics.cgmlogger.app.model.PersistedRawCgmRecord
import com.kludgenics.cgmlogger.extension.where
import io.realm.Realm
import io.realm.RealmChangeListener
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalTime
import org.joda.time.Period

/**
 * Created by matthias on 4/19/16.
 */

class Timeline(val realm: Realm, val endTime: DateTime, val period: Period): AnkoLogger, DataBindingObservable, RealmChangeListener {

    override var mCallbacks: PropertyChangeRegistry? = null

    private val results = realm.where<PersistedRawCgmRecord> {
        between("_date", (endTime - period).toDate(), endTime.toDate())
        greaterThan("_glucose", 38)
    }.findAllSorted("_date")
    init {
        results.addChangeListener { this }
    }

    val firstTime = (endTime - period).millis
    val entries: List<Entry> get() {
        return results.filterNot { it._glucose == null }
                      .map { Entry(it._glucose!!.toFloat(), (it.time.toDateTime().millis - firstTime).toInt() / 60000) }
    }

    val labels: List<String> get() {
        val times = arrayListOf<String>()
        for (time in firstTime .. endTime.millis step 60000L)
            times.add(LocalTime((time).toLong(), DateTimeZone.UTC).toString())
        return times
    }

    @get:Bindable
    val bgLineData: LineData get() {
        val data = LineData(labels, LineDataSet(entries, "glucose"))
        data.setDrawValues(false)
        return data
    }

    override fun onChange() {
        info("Notifying timeline change")
        notifyPropertyChanged(BR.bgLineData)
    }
}