package com.kludgenics.cgmlogger.app

import android.content.Context
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
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Created by matthias on 3/18/16.
 */
object DeviceSync {

    interface OnCompletionListner {
        fun onComplete(therapyTimeline: TherapyTimeline)
    }

    val listeners: MutableList<OnCompletionListner> = ArrayList()

    val syncExecutorService = Executors.newCachedThreadPool()
    var updating: Boolean = false

    @Synchronized
    fun addOnCompletionListener (onCompletionListner: OnCompletionListner) {
        if (!listeners.contains(onCompletionListner))
            listeners.add(onCompletionListner)
    }

    @Synchronized
    fun removeOnCompletionListener (onCompletionListner: OnCompletionListner) {
        if (listeners.contains(onCompletionListner))
            listeners.remove(onCompletionListner)
    }

    @Synchronized
    fun notifyOnCompletionListners (therapyTimeline: TherapyTimeline) =
        listeners.forEach { it.onComplete(therapyTimeline) }

    private fun downloadTandem(therapyTimeline: TherapyTimeline, device: TandemPump,
                               fetchPredicate: (Record) -> Boolean): Record? {
        therapyTimeline.merge(fetchPredicate,
                device.basalRecords, device.bolusRecords,
                device.smbgRecords, device.consumableRecords,
                device.profileRecords)
        val source = device.bolusRecords.first().source
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

    private fun updateStatus(device: Device, record: Record?) {
        val realm = Realm.getDefaultInstance()
        realm.use {
            realm.create<RealmStatus> {
                if (record != null) {
                    modificationTime = record.time.toDate()
                    statusText = Status.SUCCESS
                    serialNumber = device.serialNumber
                } else {
                    modificationTime = Date()
                    statusText = Status.FAILURE
                    serialNumber = device.serialNumber
                }
            }
        }
    }

    fun sync(context: Context, onComplete: ((TherapyTimeline?)->Unit)? = null): TherapyTimeline {
        val therapyTimeline = ConcurrentSkipListTherapyTimeline()
        println("in sync")
        if (!updating) {
            context.async() {
                updating = true
                val devices = AndroidDeviceHelper.getDevices(context)
                devices.map {
                    deviceEntry ->
                    syncDevice(context, therapyTimeline, deviceEntry)
                }.map { completedSerial ->
                    println(completedSerial.get())
                }.forEach { println(it); println(therapyTimeline.events.count()) }
                updating = false
                onComplete?.invoke(therapyTimeline)
            }
        } else {
            onComplete?.invoke(null)
        }
        return therapyTimeline
    }

    fun syncDevice(context: Context, therapyTimeline: ConcurrentSkipListTherapyTimeline, deviceEntry: AndroidDeviceHelper.DeviceEntry): Future<String> {
        println("Syncing ${deviceEntry.device.serialNumber}")
        return context.asyncResult (syncExecutorService) {
            try {
                deviceEntry.serialConnection.use {
                    val updateTime = /*getLatestSuccessFor(deviceEntry.device) ?:*/ LocalDateTime.now() - Period.days(3)
                    val fetchPredicate = { record: Record -> record.time > LocalDateTime(updateTime) }
                    val latestEvent = when (deviceEntry.device) {
                        is DexcomG4 -> downloadDexcomG4(therapyTimeline, deviceEntry.device as DexcomG4,
                                fetchPredicate)
                        is TandemPump -> downloadTandem(therapyTimeline, deviceEntry.device as TandemPump,
                                fetchPredicate)
                        else -> null
                    }
                    println("sync of ${deviceEntry.device.serialNumber} ${latestEvent?.time} done")
                    updateStatus(deviceEntry.device, latestEvent)
                    deviceEntry.device.serialNumber
                }
            } catch (e: Exception) {
                println("Exception caught: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
    }

    private fun getLatestSuccessFor(device: Device): Date? {
        val realm = Realm.getDefaultInstance()
        realm.use {
            val updates = realm.where<RealmStatus> {
                equalTo("serialNumber", device.serialNumber)
                equalTo("statusText", Status.SUCCESS)
            }.findAllSorted("modificationTime", Sort.DESCENDING)
            return updates.firstOrNull()?.modificationTime
        }
    }
}