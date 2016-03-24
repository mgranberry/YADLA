package com.kludgenics.cgmlogger.app

import com.kludgenics.alrightypump.cloud.nightscout.*
import com.kludgenics.alrightypump.device.dexcom.g4.DexcomCgmRecord
import com.kludgenics.alrightypump.therapy.*
import com.kludgenics.cgmlogger.app.model.RecordUtil
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.joda.time.LocalDateTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * Created by matthias on 3/23/16.
 */

class RealmTherapyTimeline: TherapyTimeline {
    override val events: Sequence<Record>
        get() = throw UnsupportedOperationException()
    override val bolusEvents: Sequence<BolusRecord>
        get() = throw UnsupportedOperationException()
    override val glucoseEvents: Sequence<GlucoseRecord>
        get() = throw UnsupportedOperationException()
    override val basalEvents: Sequence<BasalRecord>
        get() = throw UnsupportedOperationException()

    override fun events(start: LocalDateTime, end: LocalDateTime): Collection<Record> {
        throw UnsupportedOperationException()
    }

    override fun merge(vararg additionalEvents: Sequence<Record>) {
        additionalEvents.forEach {
            records ->
            records.forEach { record ->
                RecordUtil.createOrUpdateRecord(record)
            }
        }
        throw UnsupportedOperationException()
    }

    override fun merge(predicate: (Record) -> Boolean, vararg additionalEvents: Sequence<Record>) {
        throw UnsupportedOperationException()
    }

}

object NightscoutSync {
    fun sync(timeline: TherapyTimeline?) {
        println("Sync complete, received ${timeline?.events?.count()}")
        val nightscout_url = "https://12345678901234@omnominable.granberrys.us/"
        try {
            val nightscout = Nightscout(HttpUrl.parse(nightscout_url), OkHttpClient())
            if (timeline != null)
                nightscout.postRecords(timeline.events, object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {

                    }

                    override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                        println("Throwable2: $t ${t?.message} ${t?.cause}")
                        t?.printStackTrace()
                        println("cause")
                        t?.cause?.printStackTrace()
                    }
                })
        } catch (e: Exception) {
            println("Exception $e")
            e.printStackTrace()
        }
    }

}