package com.kludgenics.cgmlogger.app

import android.app.Application
import android.util.Log
import com.kludgenics.cgmlogger.extension.where
import com.kludgenics.cgmlogger.model.math.agp.CachedDatePeriodAgp
import com.kludgenics.cgmlogger.model.math.bgi.CachedBgi
import com.kludgenics.cgmlogger.model.math.trendline.CachedPeriod
import io.realm.Realm
import io.realm.RealmConfiguration
import net.danlew.android.joda.JodaTimeAndroid
import org.jetbrains.anko.async
import java.io.File

/**
 * Created by matthiasgranberry on 5/28/15.
 */
public class LoggerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("LoggerApplication", "start")
        JodaTimeAndroid.init(this);
        // should throw as migration is required
        Log.d("LoggerApplication", "trying realm for migration")
        val configuration = RealmConfiguration.Builder(this).build()
        Realm.setDefaultConfiguration(configuration)
        Realm.getDefaultInstance().close()
        // delete old cache entries for testing

        if (false) {
            val arr = Realm.getDefaultInstance()
            arr.beginTransaction()
            arr.where<CachedDatePeriodAgp> { this }.findAll().clear()
            arr.where<CachedBgi> { this }.findAll().clear()
            arr.where<CachedPeriod> { this }.findAll().clear()
            arr.commitTransaction()
            arr.close()
        }

        async {
            val realm = Realm.getDefaultInstance()
            realm.use {
                var toFile = File("/sdcard/cgm.realm")
                toFile.delete()
                Log.i("LoggerApplication", "Copying realm file")
                realm.writeCopyTo(File("/sdcard/cgm.realm"))
                Log.i("LoggerApplication", "Realm is at: ${realm.path}")
            }

        }
        Log.d("LoggerApplication", "succeeded")
    }
}
