package com.kludgenics.cgmlogger.app

import android.app.Application
import android.util.Log
import com.kludgenics.cgmlogger.extension.where
import com.kludgenics.cgmlogger.model.math.agp.CachedDatePeriodAgp
import com.kludgenics.cgmlogger.model.math.bgi.CachedBgi
import com.kludgenics.cgmlogger.model.math.trendline.CachedPeriod
import io.realm.Realm
import io.realm.RealmConfiguration
// import net.danlew.android.joda.JodaTimeAndroid
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.async
import org.jetbrains.anko.debug
import org.jetbrains.anko.info
import java.io.File
import io.fabric.sdk.android.Fabric;
import com.crashlytics.android.Crashlytics;
import com.kludgenics.alrightypump.android.AndroidDeviceHelper
import com.kludgenics.cgmlogger.extension.transaction
import net.danlew.android.joda.JodaTimeAndroid

/**
 * Created by matthiasgranberry on 5/28/15.
 */
public class LoggerApplication : Application(), AnkoLogger {
    override fun onCreate() {
        super.onCreate()
        debug("start")
        JodaTimeAndroid.init(this);
        Fabric.with(this, Crashlytics());
        // should throw as migration is required
        debug("trying realm for migration")
        val configuration = RealmConfiguration.Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build()
        Realm.setDefaultConfiguration(configuration)
        Realm.getDefaultInstance().close()
        // delete old cache entries for testing

        if (false) {
            val arr = Realm.getDefaultInstance()
            arr.use {
                arr.transaction {
                    arr.where<CachedDatePeriodAgp> { this }.findAll().clear()
                    arr.where<CachedBgi> { this }.findAll().clear()
                    arr.where<CachedPeriod> { this }.findAll().clear()
                }
            }
        }

        async {
            val realm = Realm.getDefaultInstance()
            realm.use {
                var toFile = File("/sdcard/cgm.realm")
                toFile.delete()
                info("Copying realm file")
                realm.writeCopyTo(File("/sdcard/cgm.realm"))
                info("Realm is at: ${realm.path}")
            }

        }
        debug("succeeded")
    }
}
