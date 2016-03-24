package com.kludgenics.cgmlogger.app

import android.content.Context
import android.hardware.usb.UsbDevice
import com.kludgenics.alrightypump.android.AndroidDeviceHelper
import com.kludgenics.alrightypump.device.Device
import com.kludgenics.alrightypump.device.dexcom.g4.DexcomG4
import com.kludgenics.alrightypump.device.tandem.TandemPump
import com.kludgenics.alrightypump.therapy.ConcurrentSkipListTherapyTimeline
import com.kludgenics.alrightypump.therapy.Record
import com.kludgenics.alrightypump.therapy.TherapyTimeline
import com.kludgenics.cgmlogger.app.viewmodel.RealmStatus
import com.kludgenics.cgmlogger.app.viewmodel.Status
import com.kludgenics.cgmlogger.extension.create
import com.kludgenics.cgmlogger.extension.where
import io.realm.Realm
import io.realm.Sort
import org.jetbrains.anko.async
import org.jetbrains.anko.asyncResult
import org.joda.time.LocalDateTime
import org.joda.time.Period
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Created by matthias on 3/18/16.
 */
object DeviceSync {

    val syncExecutorService = Executors.newCachedThreadPool()

    private fun downloadTandem(therapyTimeline: TherapyTimeline, device: TandemPump,
                               fetchPredicate: (Record) -> Boolean): Record? {
        therapyTimeline.merge(fetchPredicate,
                device.basalRecords, device.bolusRecords,
                device.smbgRecords, device.consumableRecords,
                device.profileRecords)
        val source = device.records.filterIsInstance<Record>().first().source
        println("source=$source")
        return therapyTimeline.events.filter { it.source == source }.lastOrNull()
    }

    private fun downloadDexcomG4(therapyTimeline: TherapyTimeline, device: DexcomG4,
                                 fetchPredicate: (Record) -> Boolean): Record? {
        therapyTimeline.merge(fetchPredicate,
                device.cgmRecords, device.smbgRecords, device.calibrationRecords,
                device.eventRecords, device.consumableRecords)
        val source = device.cgmRecords.first().source
        return therapyTimeline.events.filter { it.source == source }.lastOrNull()
    }

    private fun createStatus(realm: Realm, device: Device): Int {
        return realm.create<RealmStatus> {
            syncStartTime = Date()
            modificationTime = syncStartTime
            serialNumber = device.serialNumber
            statusCode = Status.CODE_DEVICE_READ_IN_PROGRESS
            statusText = Status.IN_PROGRESS
            icon = R.drawable.bluetooth_circle
            active = true
        }.syncId
    }

    private fun getLatestSuccessFor(device: Device): Date? {
        val realm = Realm.getDefaultInstance()
        realm.use {
            val updates = realm.where<RealmStatus> {
                equalTo("serialNumber", device.serialNumber)
                equalTo("statusText", Status.SUCCESS)
            }.findAllSorted("modificationTime", Sort.DESCENDING)
            return updates.firstOrNull()?.syncStartTime
        }
    }

    private fun updateStatus(realm: Realm, syncId: Int, statusCode: Int, latestRecordTime: Date? = null, inProgress: Boolean = false) {
        val status = realm.where<RealmStatus> {
            equalTo("syncId", syncId)
        }.findAllSorted("modificationTime", Sort.DESCENDING).firstOrNull()
        if (status != null) {
            realm.beginTransaction()
            status.modificationTime = Date()
            status.statusCode = statusCode
            status.statusText = when (status.statusCode) {
                Status.CODE_DEVICE_READ_IN_PROGRESS -> Status.IN_PROGRESS
                Status.CODE_SUCCESS -> Status.SUCCESS
                Status.CODE_FAILURE -> Status.FAILURE
                else -> "Unknown status"
            }
            status.active = inProgress
            status.latestRecordTime = latestRecordTime
            realm.commitTransaction()
        }
    }

    fun sync(context: Context, device: UsbDevice): TherapyTimeline {
        val therapyTimeline = ConcurrentSkipListTherapyTimeline()
        val deviceEntry = AndroidDeviceHelper.getDeviceEntry(context, device)
        if (deviceEntry != null)
            syncDevice(context, therapyTimeline, deviceEntry).get()
        return therapyTimeline
    }

    fun stopDeviceSync(device: UsbDevice) {
        val deviceKey = device.deviceName
    }

    fun syncDevice(context: Context, therapyTimeline: ConcurrentSkipListTherapyTimeline, deviceEntry: AndroidDeviceHelper.DeviceEntry): Future<String> {
        println("Syncing ${deviceEntry.device.serialNumber}")
        return context.asyncResult (syncExecutorService) {
            try {
                deviceEntry.serialConnection.use {
                    val realm = Realm.getDefaultInstance()
                    realm.use {
                        val syncId = createStatus(realm, deviceEntry.device)
                        try {
                            val updateTime = getLatestSuccessFor(deviceEntry.device) ?: LocalDateTime.now() - Period.days(3)

                            val fetchPredicate = { record: Record -> record.time > LocalDateTime(updateTime) }
                            val latestEvent = when (deviceEntry.device) {
                                is DexcomG4 -> downloadDexcomG4(therapyTimeline, deviceEntry.device as DexcomG4,
                                        fetchPredicate)
                                is TandemPump -> downloadTandem(therapyTimeline, deviceEntry.device as TandemPump,
                                        fetchPredicate)
                                else -> null
                            }
                            println("sync of ${deviceEntry.device.serialNumber} ${latestEvent?.time} done")
                            updateStatus(realm, syncId, Status.CODE_SUCCESS, latestEvent?.time?.toDate(), inProgress = false)
                        } catch (e: ArrayIndexOutOfBoundsException) {
                            println("sync of ${deviceEntry.device.serialNumber} failed.")
                            updateStatus(realm, syncId, Status.CODE_FAILURE, null, false)
                        }
                    }
                    deviceEntry.device.serialNumber
                }
            } catch (e: Exception) {
                println("Exception caught: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
    }
}