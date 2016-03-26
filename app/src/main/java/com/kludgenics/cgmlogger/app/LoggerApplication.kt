package com.kludgenics.cgmlogger.app

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import io.fabric.sdk.android.Fabric;
import com.crashlytics.android.Crashlytics;
import net.danlew.android.joda.JodaTimeAndroid

/**
 * Created by matthiasgranberry on 5/28/15.
 */
class LoggerApplication : Application(), AnkoLogger {
    override fun onCreate() {
        super.onCreate()
        debug("start")
        Fabric.with(this, Crashlytics());
        // should throw as migration is required
        debug("trying realm for migration")
        JodaTimeAndroid.init(this@LoggerApplication);

        val configuration = RealmConfiguration.Builder(this@LoggerApplication)
                .deleteRealmIfMigrationNeeded()
                .build()
        Realm.setDefaultConfiguration(configuration)
    }
}
