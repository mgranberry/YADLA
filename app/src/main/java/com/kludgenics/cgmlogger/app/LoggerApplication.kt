package com.kludgenics.cgmlogger.app

import android.app.Application
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import io.fabric.sdk.android.Fabric;
import com.crashlytics.android.Crashlytics;
import com.kludgenics.alrightypump.android.BleClient
import com.kludgenics.alrightypump.therapy.GlucoseRecord
import com.kludgenics.cgmlogger.app.model.*
import com.squareup.otto.Produce
import net.danlew.android.joda.JodaTimeAndroid
import com.kludgenics.cgmlogger.extension.*
import io.realm.*
import org.jetbrains.anko.collections.forEachReversed
import org.jetbrains.anko.info
import java.util.*

/**
 * Created by matthiasgranberry on 5/28/15.
 */
class LoggerApplication : Application(), AnkoLogger {
    val realm: Realm by lazy { Realm.getDefaultInstance() }

    override fun onCreate() {
        super.onCreate()
        debug("start")
        JodaTimeAndroid.init(this@LoggerApplication);
        Fabric.with(this, Crashlytics());

        val configuration = RealmConfiguration.Builder(this@LoggerApplication)
                .schemaVersion(YadlaMigration.CURRENT_VERSION)
                .migration(YadlaMigration())
                .build()
        Realm.setDefaultConfiguration(configuration)
        EventBus.register(this)
    }

    @Suppress("unused")
    @Produce fun produceGlucoseRecord(): Pair<PersistedRawCgmRecord,PersistedRawCgmRecord>? {
        val baseRecords = realm.where<PersistedRecord> {
            equalTo("_eventType", EventType.GLUCOSE)
        }.findAllSorted("_date", Sort.DESCENDING)
        if (baseRecords.count() >= 2) {
            val records = baseRecords.take(2).mapNotNull { TypedRecord.inflate(realm, it) as? PersistedRawCgmRecord }
            return records.component2() to records.component1()
        } else
            return null
    }

    override fun onTerminate() {
        realm.close()
        EventBus.unregister(this)
        super.onTerminate()
    }
}