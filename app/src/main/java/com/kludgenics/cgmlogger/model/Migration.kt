package com.kludgenics.cgmlogger.model

import com.kludgenics.cgmlogger.model.glucose.BloodGlucoseRecord
import com.kludgenics.cgmlogger.model.nightscout.CalibrationEntry
import io.realm.Realm
import io.realm.RealmMigration
import io.realm.internal.Table

/**
 * Created by matthiasgranberry on 6/3/15.
 */
public class Migration() : RealmMigration {
    override fun execute(realm: Realm, version: Long): Long {
        var index = version
        val current = 1
        while (index < current) {
            index = when (version) {
                0L, 1L -> {
                    val tableClasses = arrayOf(javaClass<BloodGlucoseRecord>())
                    tableClasses.forEach {
                        val t: Table = realm.getTable(it)
                        t.setPrimaryKey("id")
                        t.close()
                    }
                    version + 1
                }
                else -> version
            }
        }
        return index
    }
}