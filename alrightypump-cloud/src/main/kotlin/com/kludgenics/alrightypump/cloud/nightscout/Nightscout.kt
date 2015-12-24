package com.kludgenics.alrightypump.cloud.nightscout

import com.kludgenics.alrightypump.cloud.nightscout.records.therapy.NightscoutCgmRecord
import com.kludgenics.alrightypump.cloud.nightscout.records.therapy.NightscoutRecord
import com.kludgenics.alrightypump.device.dexcom.g4.DexcomCgmRecord
import com.kludgenics.alrightypump.therapy.*
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.okhttp.*
import okio.ByteString
import org.joda.time.DateTime
import org.joda.time.Instant
import retrofit.MoshiConverterFactory
import retrofit.Retrofit
import java.security.MessageDigest
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by matthias on 11/29/15.
 */

class Nightscout @Inject constructor (@Named("Nightscout") url: HttpUrl,
                                      okHttpClient: OkHttpClient) {

    companion object {
        const final val FETCH_SIZE = 50000
    }

    private val retrofit: Retrofit
    private val nightscoutApi: NightscoutApi

    public val entries: Sequence<NightscoutRecord> get() = entries().mapNotNull {
        when (it.rawEntry) {
            is NightscoutSgvJson -> NightscoutCgmRecord(it.rawEntry)
            else -> null
        }
    }

    public fun entries(range: ClosedRange<Instant> = InstantRange(Instant.parse("2008-01-01T01:00:00Z"),
            Instant.now())): Sequence<NightscoutEntryJson> {
        return (RestCallSequence(range) {
            start, end ->
            nightscoutApi
                    .getRecordsBetween(start.millis, end.millis, FETCH_SIZE)
                    .execute()
                    .body()
        })
    }

    public val treatments: Sequence<NightscoutTreatment> get() = treatments()
    public fun treatments(range: ClosedRange<Instant> = InstantRange(Instant.parse("2008-01-01T01:00:00Z"),
                          Instant.now())): Sequence<NightscoutTreatment> {
        return RestCallSequence(range) {
            start, end ->
            nightscoutApi
                    .getTreatmentsBetween(start.toString(), end.toString(), FETCH_SIZE)
                    .execute()
                    .body()
        }
    }

    public fun postRecords(records: Sequence<Record>) {
        var (treatmentRecords, entryRecords) = records.flatMap {
            when (it) {
                is CalibrationRecord -> sequenceOf(NightscoutEntryJson(NightscoutCalJson(it)))
                is DexcomCgmRecord -> sequenceOf(NightscoutEntryJson(NightscoutSgvJson(it))) // TODO this is the only place where there is a device dependency :(
                is RawCgmRecord -> sequenceOf(NightscoutEntryJson(NightscoutSgvJson(it)))
                is CgmRecord -> sequenceOf(NightscoutEntryJson(NightscoutSgvJson(it)))
                is SmbgRecord -> sequenceOf(NightscoutEntryJson(NightscoutMbgJson(it)))
                is FoodRecord,
                is CgmInsertionRecord,
                is TemporaryBasalStartRecord,
                is TermoraryBasalEndRecord,
                is CannulaChangedRecord,
                is CartridgeChangeRecord,
                is CannulaChangedRecord,
                    // is ScheduledBasalRecord,
                is BolusRecord -> {
                    val treatment = NightscoutTreatment(HashMap())
                    treatment.applyRecord(it)
                    sequenceOf(treatment)
                }
                else -> emptyList<Record>().asSequence()
            }
        }.partition { it is NightscoutApiTreatment }
        while (!entryRecords.isEmpty()) {
            val r = entryRecords.filterIsInstance<NightscoutEntryJson>().mapIndexed {
                i, nightscoutEntryJson ->
                i to nightscoutEntryJson
            }.partition {
                it.first < 1000
            }
            val batch = r.first.map { it.second }.toArrayList()
            entryRecords = r.second.map { it.second }
            try {
                if (!batch.isEmpty())
                    nightscoutApi.postRecords(batch).enqueue(object : retrofit.Callback<MutableList<NightscoutEntryJson>> {
                        override fun onResponse(response: retrofit.Response<MutableList<NightscoutEntryJson>>?, retrofit: Retrofit?) {
                        }

                        override fun onFailure(t: Throwable?) {
                        }

                    })
            } catch (e: JsonDataException) {

            }
        }
        val t = treatmentRecords.filterIsInstance<NightscoutTreatment>().fold(arrayListOf<NightscoutTreatment>()) {
            arrayList: ArrayList<NightscoutTreatment>, nightscoutTreatment: NightscoutTreatment ->
            if (arrayList.size >= 1) {
                nightscoutApi.postTreatments(arrayList).enqueue(object : retrofit.Callback<ResponseBody> {
                    override fun onResponse(response: retrofit.Response<ResponseBody>?, retrofit: Retrofit?) {
                    }

                    override fun onFailure(t: Throwable?) {
                    }

                })
                arrayList.clear()
            }
            arrayList.add(nightscoutTreatment)
            arrayList
        }
        if (t.isNotEmpty()) {
            nightscoutApi.postTreatments(t).enqueue(object : retrofit.Callback<ResponseBody> {
                override fun onResponse(response: retrofit.Response<ResponseBody>?, retrofit: Retrofit?) {
                }

                override fun onFailure(t: Throwable?) {
                }

            })
        }
    }

    private class RestCallSequence<T:NightscoutApiEntry>(val range: ClosedRange<Instant>,
                                                         val call: (Instant, Instant) -> List<T>) : Sequence<T> {
        companion object {
            const val batchSize = 100000
        }

        override fun iterator(): Iterator<T> {
            return object : Iterator<T> {

                var current = range.endInclusive
                var currentRange  = range.endInclusive.toRecordRange()
                var currentRecords = call(currentRange.start, currentRange.endInclusive).filter { range.contains(it.date) }
                var eventIterator: Iterator<T> = currentRecords.iterator()

                init {
                    updateIterator()
                }

                private fun updateIterator(): Boolean {
                    if (range.contains(currentRange.start)) {
                        currentRange = currentRange.start.minus(1).toRecordRange()
                        currentRecords = call(currentRange.start, currentRange.endInclusive)
                        if (currentRecords.isEmpty())
                            return false
                        current = currentRecords.last().date
                        eventIterator = currentRecords.iterator()
                        return true
                    } else
                        return false
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

data class InstantRange(override val start: Instant,
                        override val endInclusive: Instant) : ClosedRange<Instant>


public fun Instant.toRecordRange(): ClosedRange<Instant> {
    val now = DateTime.now()
    val dateStart = (this - 1).toDateTime().withTimeAtStartOfDay()

    val range = if (now.weekOfWeekyear().roundFloorCopy() == dateStart.weekOfWeekyear().roundFloorCopy()) {
        println("days")
        InstantRange(dateStart.toInstant(), dateStart.plusDays(1).toInstant() - 1)
    } else if (now.monthOfYear().roundFloorCopy() == dateStart.monthOfYear().roundFloorCopy()) {
        println("weeks")
        InstantRange(dateStart.weekOfWeekyear().roundFloorCopy().toInstant(),
                dateStart.weekOfWeekyear().roundCeilingCopy().toInstant() - 1)
    } else {
        println("months")
        InstantRange(dateStart.monthOfYear().roundFloorCopy().toInstant(),
                dateStart.monthOfYear().roundCeilingCopy().toInstant() - 1)
    }
    println("Calculated range $range from $this")
    return range
}
