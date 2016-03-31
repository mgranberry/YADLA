package com.kludgenics.alrightypump.cloud.nightscout

import com.kludgenics.alrightypump.DateTimeChangeRecord
import com.kludgenics.alrightypump.cloud.mutableChunked
import com.kludgenics.alrightypump.cloud.nightscout.records.json.NightscoutEntry
import com.kludgenics.alrightypump.device.ContinuousGlucoseMonitor
import com.kludgenics.alrightypump.device.Glucometer
import com.kludgenics.alrightypump.device.InsulinPump
import com.kludgenics.alrightypump.device.dexcom.g4.DexcomCgmRecord
import com.kludgenics.alrightypump.therapy.*
import com.squareup.moshi.Moshi
import okhttp3.*
import okio.ByteString
import org.joda.time.Chronology
import org.joda.time.Duration
import org.joda.time.Instant
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by matthias on 11/29/15.
 */

class Nightscout @Inject constructor (@Named("Nightscout") val url: HttpUrl,
                                      okHttpClient: OkHttpClient) : InsulinPump,
        ContinuousGlucoseMonitor,
        Glucometer {

    data class ChunkedCall<T>(val argumentList: List<T>, val call: Call<ResponseBody>)

    override val timeCorrectionOffset: Duration?
        get() = Duration.ZERO
    override val profileRecords: Sequence<ProfileRecord>
        get() = throw UnsupportedOperationException()
    override val chronology: Chronology
        get() = throw UnsupportedOperationException()
    override val serialNumber: String
        get() = url.toString()
    override val bolusRecords: Sequence<BolusRecord>
        get() = throw UnsupportedOperationException()
    //treatments.filter { it.insulin != null }.map { null }
    override val basalRecords: Sequence<BasalRecord>
        get() = throw UnsupportedOperationException()
    override val consumableRecords: Sequence<ConsumableRecord>
        get() = throw UnsupportedOperationException()
    override val smbgRecords: Sequence<NightscoutApiMbgEntry>
        get() = entries.filterIsInstance<NightscoutApiMbgEntry>()
    override val dateTimeChangeRecords: Sequence<DateTimeChangeRecord>
        get() = throw UnsupportedOperationException()
    override val outOfRangeHigh: Double
        get() = throw UnsupportedOperationException()
    override val outOfRangeLow: Double
        get() = throw UnsupportedOperationException()
    override val cgmRecords: Sequence<NightscoutApiSgvEntry>
        get() = entries.filterIsInstance<NightscoutApiSgvEntry>()

    companion object {
        const final val FETCH_SIZE = 50000
    }

    private val retrofit: Retrofit
    private val nightscoutApi: NightscoutApi

    fun status(): retrofit2.Response<NightscoutStatus> {
        val call = nightscoutApi.getStatus()
        return try {
            call.execute()
        } catch (e: InterruptedException) {
            call.cancel()
            throw e
        }
    }

    fun verifyAuth(): retrofit2.Response<NightscoutCodeVerification> {
        val call = nightscoutApi.verifyAuth()
        return try {
            call.execute()
        } catch (e: InterruptedException) {
            call.cancel()
            throw e
        }
    }

    val entries: Sequence<Record> get() = entries().mapNotNull {
        when (it.rawEntry) {
            is Record -> it
            else -> null
        }
    }

    fun entries(range: ClosedRange<Instant> = RestCallSequence.InstantRange(Instant.parse("2008-01-01T01:00:00Z"),
            Instant.now())): Sequence<NightscoutEntryJson> {
        return (RestCallSequence(range) {
            start, end ->
            nightscoutApi
                    .getRecordsBetween(start.millis, end.millis, FETCH_SIZE)
                    .execute()
                    .body()
        })
    }

    val treatments: Sequence<NightscoutTreatment> get() = treatments()
    fun treatments(range: ClosedRange<Instant> = RestCallSequence.InstantRange(Instant.parse("2008-01-01T01:00:00Z"),
            Instant.now())): Sequence<NightscoutTreatment> {
        return RestCallSequence(range) {
            start, end ->
            nightscoutApi
                    .getTreatmentsBetween(start.toString(), end.toString(), FETCH_SIZE)
                    .execute()
                    .body()
        }
    }

    fun <T: Record> prepareUploads(records: Sequence <T> ): Sequence<ChunkedCall<T>> {
        var (treatmentRecords, entryRecords: List<Pair<T, Record>>) = records.flatMap {
            when (it) {
                is CalibrationRecord -> sequenceOf(it as T to NightscoutEntryJson(NightscoutCalJson(it)))
                is DexcomCgmRecord -> sequenceOf(it as T to NightscoutEntryJson(NightscoutSgvJson(it))) // TODO this is the only place where there is a device dependency :(
                is RawCgmRecord -> sequenceOf(it as T to NightscoutEntryJson(NightscoutSgvJson(it)))
                is CgmRecord -> sequenceOf(it as T to NightscoutEntryJson(NightscoutSgvJson(it)))
                is SmbgRecord -> sequenceOf(it as T to NightscoutEntryJson(NightscoutMbgJson(it)))
                is FoodRecord,
                is CgmInsertionRecord,
                is TemporaryBasalStartRecord,
                is TemporaryBasalEndRecord,
                is SuspendedBasalRecord,
                is CartridgeChangeRecord,
                is CannulaChangedRecord,
                    // is ScheduledBasalRecord,
                is BolusRecord -> {
                    val treatment = NightscoutTreatment(HashMap())
                    treatment.applyRecord(it)
                    sequenceOf(it as T to treatment)
                }
                else -> emptySequence<Pair<T, Record>>()
            }
        }.partition { it.second is NightscoutApiTreatment }

        val entryChunks = entryRecords.filter { it.second is NightscoutEntryJson }.asSequence().mutableChunked(1000)
        val treatmentChunks: Sequence<MutableList<Pair<T, Record>>> = treatmentRecords.filter { it.second is NightscoutTreatment }.asSequence().mutableChunked(1000)

        return entryChunks.map {
            ChunkedCall(it.map { it.first }, nightscoutApi.postRecords(it.map { it.second as NightscoutEntryJson }.toMutableList()))
        }.plus(treatmentChunks.map {
            ChunkedCall(it.map { it.first }, nightscoutApi.postTreatments(it.map { it.second as NightscoutTreatment }.toMutableList()))
        })
    }


    private fun calculateSecretHash(secret: String): String {
        val digestBytes = MessageDigest.getInstance("SHA-1")
                .digest(secret.toByteArray(Charset.forName("utf8")));
        return ByteString.of(*digestBytes).hex()
    }

    private class ApiSecretInterceptor(private val apiSecretHash: String) : Interceptor {
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
        retrofit = Retrofit.Builder().addConverterFactory(MoshiConverterFactory.create(moshiBuilder.build()))
                .baseUrl(baseUrl)
                .client(okHttpClient.newBuilder().addInterceptor(ApiSecretInterceptor(calculateSecretHash(apiSecret))).build())
                .build()
        nightscoutApi = retrofit.create(NightscoutApi::class.java)
    }
}

