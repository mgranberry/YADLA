package com.kludgenics.cgmlogger.app

import android.app.Application
import android.app.usage.UsageEvents
import android.util.Log
import com.kludgenics.cgmlogger.model.Migration
import com.kludgenics.cgmlogger.util.FileUtil

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.exceptions.RealmMigrationNeededException
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
        try {
            // should throw as migration is required
            Log.d("LoggerApplication", "trying realm for migration")
            val configuration = RealmConfiguration.Builder(this).build()
            Realm.setDefaultConfiguration(configuration)
            Realm.getDefaultInstance().close()
            val r = async {
                val realm = Realm.getDefaultInstance()
                realm.use {
                    var toFile = File("/sdcard/cgm.realm")
                    toFile.delete()
                    Log.i("LoggerApplication", "Copying realm file")
                    realm.writeCopyTo(File("/sdcard/cgm.realm"))
                    Log.i("LoggerApplication", "Realm is at: ${realm.getPath()}")
                }

            }
            Log.d("LoggerApplication", "succeeded")

        } catch (ex: RealmMigrationNeededException) {
            Log.d("LogerApplication", "migrating")
            Realm.migrateRealmAtPath("${getFilesDir()}/${Realm.DEFAULT_REALM_NAME}", Migration())
            Log.d("LogerApplication", "migrated")

        }
    }
}
