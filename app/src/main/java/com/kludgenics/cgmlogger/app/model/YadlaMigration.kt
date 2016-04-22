package com.kludgenics.cgmlogger.app.model

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.joda.time.LocalTime
import java.util.*


class YadlaMigration : RealmMigration, AnkoLogger {
    companion object {
        const val CURRENT_VERSION = 14L
    }
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var currentVersion = oldVersion
        val schema = realm.schema
        info("beginning migration from $oldVersion to $newVersion")
        while (currentVersion != newVersion && currentVersion != -1L) {
            info("beginning migration from $oldVersion to $newVersion")

            currentVersion = when(currentVersion) {
                0L,
                2L, 3L, 4L, 5L, 6L -> {
                    try { schema.remove("DeviceSyncCard") } catch (e: Exception) {};
                    try { schema.remove("BloodGlucoseCard") } catch (e: Exception) {};

                    schema.create("DeviceSyncCard")
                            .addField("key", String::class.java, FieldAttribute.INDEXED, FieldAttribute.REQUIRED)
                            .addField("time", Date::class.java, FieldAttribute.INDEXED)
                            .addField("icon", Int::class.java)
                            .addField("titleText", String::class.java)
                            .addField("subtitleText", String::class.java)
                            .addField("shortText", String::class.java)
                            .addField("extraText", String::class.java)
                            .addField("type", Int::class.java)
                    schema.create("BloodGlucoseCard")
                            .addField("key", String::class.java, FieldAttribute.INDEXED, FieldAttribute.REQUIRED)
                            .addField("time", Date::class.java, FieldAttribute.INDEXED)
                            .addField("start", Date::class.java)
                            .addField("end", Date::class.java)
                            .addField("options", Int::class.java)
                            .addField("type", Int::class.java)

                    7
                }
                7L, 8L, 9L -> {
                    info("Migrating records")
                    schema.get("PersistedRawCgmRecord")
                            .addField("millisOfDay", Int::class.java)
                            .transform { it ->
                                it.set("millisOfDay", LocalTime(it.get<Date>("_date")).millisOfDay)
                            }
                    10
                }
                10L -> {
                    schema.get("PersistedRawCgmRecord").addIndex("millisOfDay")
                    11
                }
                11L, 12L -> {
                    val recordSchema = schema.get("PersistedRawCgmRecord")
                    recordSchema.addField("_glucoseInt", Int::class.java, FieldAttribute.INDEXED).transform {
                        info("Field type: ${it.getFieldType("_glucose")}")
                        val glucose = it.get<Number?>("_glucose")
                        if (glucose != null)
                            it.set("_glucoseInt", glucose.toInt())
                    }.removeField("_glucose").renameField("_glucoseInt", "_glucose")
                    13
                }
                13L -> {
                    val recordSchema = schema.get("PersistedRawCgmRecord")
                    recordSchema.setNullable("_glucose", true)
                    14L
                }
                else -> -1
            }
        }
        if (currentVersion == -1L)
            throw UnsupportedOperationException()
    }

}
