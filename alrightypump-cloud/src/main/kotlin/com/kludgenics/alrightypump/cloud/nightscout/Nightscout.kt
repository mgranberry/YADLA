package com.kludgenics.alrightypump.cloud.nightscout

import com.kludgenics.alrightypump.cloud.nightscout.records.therapy.NightscoutCgmRecord
import com.kludgenics.alrightypump.cloud.nightscout.records.therapy.NightscoutRecord
import com.kludgenics.alrightypump.device.dexcom.g4.DexcomCgmRecord
import com.kludgenics.alrightypump.therapy.*
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.okhttp.HttpUrl
import com.squareup.okhttp.Interceptor
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Response
import okio.ByteString
import org.joda.time.Instant
import retrofit.MoshiConverterFactory
import retrofit.Retrofit
import java.security.MessageDigest
import java.util.*

/**
 * Created by matthias on 11/29/15.
 */

class Nightscout(private val url: HttpUrl,
                 private val okHttpClient: OkHttpClient = OkHttpClient()) {
    private val treatmentMap = TreeMap<TherapyKey, Record>()
    private val entryMap = TreeMap<TherapyKey, Record>()
    private data class TherapyKey (val instant: Instant, val recordType: String)
    private val retrofit: Retrofit
    private val nightscoutApi: NightscoutApi
    public var overwriteEntries: Boolean = true
    public var overwriteTreatments: Boolean = false
    public var entryPageSize: Int = 576

    public val entries : Sequence<NightscoutRecord> get() = (RestCallSequence(Instant.parse("2008-01-01T01:00:00Z"),
            Instant.now(),
            entryPageSize) {
        start, end, count ->
        nightscoutApi
                .getRecordsBetween(start.millis, end.millis, count)
                .execute()
                .body()
        }).mapNotNull {
        when (it.rawEntry) {
            is NightscoutSgvJson -> NightscoutCgmRecord(it.rawEntry)
            else -> null
        }
    }

    public var treatmentPageSize: Int = 100
    public val treatments: Sequence<NightscoutTreatment> get() = RestCallSequence(Instant.parse("2008-01-01T01:00:00Z"),
            Instant.now(),
            treatmentPageSize) {
        start, end, count -> nightscoutApi
            .getTreatmentsBetween(start.toString(), end.toString(), count)
            .execute()
            .body()
    }

    public fun postRecords(records: Sequence<Record>) {
        var (treatmentRecords, entryRecords) = records.mapNotNull {
            when (it) {
                is CalibrationRecord -> NightscoutEntryJson(NightscoutCalJson(it))
                is DexcomCgmRecord -> NightscoutEntryJson(NightscoutSgvJson(it))
                is CgmRecord -> NightscoutEntryJson(NightscoutSgvJson(it))
                is FoodRecord,
                is CgmInsertionRecord,
                is TemporaryBasalRecord,
                // is ScheduledBasalRecord,
                is BolusRecord-> {
                    val treatment = NightscoutTreatment(HashMap())
                    treatment.applyRecord(it)
                    treatment
                }
                else -> null
            }
        }.partition { it is NightscoutApiTreatment }
        while (!entryRecords.isEmpty()) {
            //println("entries:$entries")
            val r = entryRecords.filterIsInstance<NightscoutEntryJson>().mapIndexed { i, nightscoutEntryJson -> i to nightscoutEntryJson }.partition { it.first < 1000 }
            val batch = r.first.map { it.second }.toArrayList()
            println("posting $batch")

            entryRecords = r.second.map { it.second }
            //println("entries: $entries")
            try {
                if (!batch.isEmpty())
                    nightscoutApi.postRecords(batch).execute()
            } catch (e: JsonDataException) {

            }
        }
        println("treatments: $treatmentRecords")
        while (!treatmentRecords.isEmpty()) {
            //println("entries:$entries")
            val r = treatmentRecords.filterIsInstance<NightscoutTreatment>().mapIndexed {
                i, treatment -> i to treatment
            }.partition { it.first < 1 }
            val batch = r.first.map { it.second }.toArrayList()
            println("posting $batch")

            treatmentRecords = r.second.map { it.second }
            try {
                if (!batch.isEmpty())
                    nightscoutApi.postTreatments(batch).execute()
            } catch (e: JsonDataException) {

            }
        }

    }

    private class RestCallSequence<T:NightscoutApiEntry>(val startInstant: Instant,
                                                         val endInstant: Instant,
                                                         val batchSize: Int,
                                                         val call: (Instant, Instant, Int) -> List<T>) : Sequence<T> {
        override fun iterator(): Iterator<T> {
            return object : Iterator<T> {

                var current = endInstant
                var currentRecords = call(startInstant, current, batchSize)
                var eventIterator: Iterator<T> = currentRecords.iterator()

                init {
                    updateIterator()
                }

                private fun updateIterator(): Boolean {
                    currentRecords = call(startInstant, current, batchSize)
                    if (currentRecords.isEmpty())
                        return false
                    current = currentRecords.last().date
                    eventIterator = currentRecords.iterator()
                    return true
                }

                override fun next(): T {
                    if (eventIterator.hasNext()) {
                        return eventIterator.next()
                    } else {
                        updateIterator()
                        return eventIterator.next()
                    }
                }

                override fun hasNext(): Boolean {
                    if (eventIterator.hasNext() == true)
                        return true
                    else {
                        return updateIterator()
                    }
                }
            }
        }
    }

    private fun calculateSecretHash(secret: String): String {
        val digestBytes = MessageDigest.getInstance("SHA-1")
                .digest(secret.toByteArray("utf8"));
        return ByteString.of(*digestBytes).hex()
    }

    private class ApiSecretIntercepter(private val apiSecretHash: String) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            return chain.proceed(request.newBuilder().header("api-secret", apiSecretHash).build())
        }
    }

    init {
        val moshiBuilder = Moshi.Builder()
        NightscoutApi.registerTypeAdapters(moshiBuilder)
        val apiSecret = url.username()
        val baseUrl = url.newBuilder().username("").build()
        okHttpClient.networkInterceptors().add(ApiSecretIntercepter(calculateSecretHash(apiSecret)))
        retrofit = Retrofit.Builder().addConverterFactory(MoshiConverterFactory.create(moshiBuilder.build()))
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build()
        nightscoutApi = retrofit.create(NightscoutApi::class.java)
    }

}