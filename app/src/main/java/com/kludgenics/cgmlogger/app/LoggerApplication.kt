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
        JodaTimeAndroid.init(this);
        //Fabric.with(this, Crashlytics());
        // should throw as migration is required
        debug("trying realm for migration")
        val configuration = RealmConfiguration.Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build()
        Realm.setDefaultConfiguration(configuration)
        Realm.getDefaultInstance().close()
    }
}
