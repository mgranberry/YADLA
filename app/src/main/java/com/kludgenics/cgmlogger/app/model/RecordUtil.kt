package com.kludgenics.cgmlogger.app.model

import com.kludgenics.alrightypump.device.dexcom.g4.DexcomCgmRecord
import com.kludgenics.alrightypump.therapy.*
import io.realm.Realm
import io.realm.RealmObject

/**
 * Created by matthias on 3/23/16.
 */

object RecordUtil {
    private fun populateCommonFields(persistedRecord: PersistedRecord, record: Record) {
        persistedRecord._id = record.id
        persistedRecord._source = record.source
        persistedRecord._date = record.time.toDate()
    }

    fun createOrUpdateRecord(record: Record) {
        val realm = Realm.getDefaultInstance()
        realm.use {
            realm.beginTransaction()
            try {
                realm.copyToRealmOrUpdate(
                        when (record) {
                            is CalibrationRecord,
                            is DexcomCgmRecord,
                            is RawCgmRecord,
                            is CgmRecord,
                            is SmbgRecord,
                            is FoodRecord,
                            is CgmInsertionRecord,
                            is TemporaryBasalStartRecord,
                            is TemporaryBasalEndRecord,
                            is SuspendedBasalRecord,
                            is CannulaChangedRecord,
                            is CartridgeChangeRecord,
                            is CannulaChangedRecord,
                                // is ScheduledBasalRecord,
                            is BolusRecord -> {
                                null as? RealmObject?
                            }
                            else -> null
                        })
                realm.commitTransaction()
            } catch (e: Exception) {
                realm.cancelTransaction()
                throw e
            }
        }

    }
}