package com.kludgenics.cgmlogger.app

import android.app.Application
import android.app.usage.UsageEvents
import android.util.Log
import com.kludgenics.cgmlogger.model.Migration

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.exceptions.RealmMigrationNeededException
import net.danlew.android.joda.JodaTimeAndroid

/**
 * Created by matthiasgranberry on 5/28/15.
 */
public class LoggerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("LoggerApplication", "start")
        JodaTimeAndroid.init(this);
        try {
            // should throw as migration is required
            Log.d("LoggerApplication", "trying realm for migration")
            val configuration = RealmConfiguration.Builder(this).build()
            Realm.setDefaultConfiguration(configuration)
            Realm.getDefaultInstance().close()

            Log.d("LoggerApplication", "succeeded")

        } catch (ex: RealmMigrationNeededException) {
            Log.d("LogerApplication", "migrating")
            Realm.migrateRealmAtPath("${getFilesDir()}/${Realm.DEFAULT_REALM_NAME}", Migration())
            Log.d("LogerApplication", "migrated")

        }
    }
}
