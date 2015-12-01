package com.kludgenics.alrightypump.cloud.nightscout

import com.kludgenics.alrightypump.cloud.nightscout.records.therapy.NightscoutCgmRecord
import com.kludgenics.alrightypump.cloud.nightscout.records.therapy.NightscoutRecord
import com.kludgenics.alrightypump.device.dexcom.g4.DexcomCgmRecord
import com.kludgenics.alrightypump.therapy.CalibrationRecord
import com.kludgenics.alrightypump.therapy.CgmRecord
import com.kludgenics.alrightypump.therapy.Record
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

    public fun postEntries(records: Sequence<Record>) {
        var postableRecords = records.mapNotNull {
            when (it) {
                is CalibrationRecord -> NightscoutCalJson(it)
                is DexcomCgmRecord -> NightscoutSgvJson(it)
                is CgmRecord -> NightscoutSgvJson(it)
                else -> null
            }
        }.map { NightscoutEntryJson(it) }.toList()
        while (!postableRecords.isEmpty()) {
            val r = postableRecords.mapIndexed { i, nightscoutEntryJson -> i to nightscoutEntryJson }.partition { it.first < 1000 }
            val batch = r.first.map { it.second }.toArrayList()
            postableRecords = r.second.map { it.second }
            try {
                if (!batch.isEmpty())
                    nightscoutApi.postRecords(batch).execute()
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