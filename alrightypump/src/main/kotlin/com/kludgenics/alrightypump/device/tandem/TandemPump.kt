package com.kludgenics.alrightypump.device.tandem

import com.kludgenics.alrightypump.DateTimeChangeRecord
import com.kludgenics.alrightypump.device.Glucometer
import com.kludgenics.alrightypump.device.InsulinPump
import com.kludgenics.alrightypump.therapy.BasalRecord
import com.kludgenics.alrightypump.therapy.ConsumableRecord
import com.kludgenics.alrightypump.therapy.ProfileRecord
import com.kludgenics.alrightypump.therapy.SmbgRecord
import okio.BufferedSink
import okio.BufferedSource
import org.joda.time.Chronology
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import org.joda.time.chrono.ISOChronology
import java.util.*

/**
 * Created by matthias on 11/19/15.
 */
class TandemPump(private val source: BufferedSource, private val sink: BufferedSink) : InsulinPump, Glucometer {
    override val timeCorrectionOffset: Duration?
        get() = Duration(commandResponse(VersionReq()).timestamp.toDateTime(), LocalDateTime.now().toDateTime())

    companion object {
        @JvmField final val EPOCH = LocalDateTime.parse("2008-01-01T00:00:00")
        const val ITERATION_STEP = 200
        val source: String get() = "alrightypump-tandem-$serial"
        private var _serial = ""
        val serial: String get() = _serial
    }

    val recordCache = TreeMap<Int, LogEvent>()
    val records: Sequence<LogEvent> get() = TslimLogSequence()


    override val chronology: Chronology
        get() = ISOChronology.getInstance()
    val versionResponse: VersionResp by lazy {VersionResp(commandResponse(VersionReq()).payload)}
    override val serialNumber: String by lazy { _serial = readSerialNumber(); _serial }
    override val outOfRangeLow: Double
        get() = 19.0
    override val outOfRangeHigh: Double
        get() = 601.0
    override val dateTimeChangeRecords: Sequence<DateTimeChangeRecord>
        get() = throw UnsupportedOperationException()
    override val smbgRecords: Sequence<SmbgRecord>
        get() = records.filterIsInstance<SmbgRecord>()
    override val bolusRecords: Sequence<TandemBolus> get() = BolusWizardAssemblingSequence()
    override val basalRecords: Sequence<BasalRecord> get() = BasalRateAssemblingSequence()
    override val consumableRecords: Sequence<ConsumableRecord>
        get() = records.filterIsInstance<ConsumableRecord>()

    /**
     * Only returns most-recent profile
     */
    override val profileRecords: Sequence<ProfileRecord>
        get() = ProfileAssemblingSequence()

    val logRange: IntRange = LogSizeResp(commandResponse(LogSizeReq()).payload).range
    val profiles: List<TandemProfile> get() = readProfiles()

    private fun readProfiles() : List<TandemProfile> {
        return IdpListResp(commandResponse(IdpListReq()).payload).idps.map { TandemProfile(readProfile(it)) }
    }

    fun readProfile(idp: Int) : IdpResp = IdpResp(commandResponse(IdpReq(idp)).payload)

    fun commandResponse(payload: TandemPayload): TandemResponse {
        val packet = TandemRequest(payload).frame
        //println("Sending: ${packet.snapshot().hex()}")
        sink.write(packet, packet.size())
        sink.emit()
        val response = TandemResponse(source)
        return response
    }

    fun readResponse(): TandemResponse {
        return TandemResponse(source)
    }

    fun readSerialNumber() : String {
        return versionResponse.pumpSN.toString()
    }

    fun readLogRecords(start: Int, end: Int): Collection<LogEvent> {
        var nRead = 0
        var nRequested = 0
        for (seqNo in start..end) {
            if (!recordCache.containsKey(seqNo)) {
                val packet = TandemRequest(LogEntrySeqReq(seqNo.toLong())).frame
                sink.write(packet, packet.size())
                sink.emit()
                nRequested += 1
                // let the pipeline fill a little and then start reading as well as writing
                if (nRequested > nRead + 2 && source.request(1)) {
                    val response = readResponse()
                    if (response.parsedPayload is LogEvent) {
                        nRead += 1
                        recordCache[response.parsedPayload.seqNo] = response.parsedPayload
                    }
                }
            }
        }
        while (nRead < nRequested) {
            val response = readResponse()
            if (response.parsedPayload is LogEvent) {
                nRead += 1
                recordCache[response.parsedPayload.seqNo] = response.parsedPayload
            }
        }
        // println("$start, $end")
        return recordCache.subMap(start, true, end, true).values
    }

    private inner class TslimLogSequence : Sequence<LogEvent> {
        override fun iterator(): Iterator<LogEvent> {
            return object : Iterator<LogEvent> {
                var eventIterator: Iterator<LogEvent>?
                var idx: Int
                val first: Int
                val last: Int

                init {
                    val log = logRange
                    first = log.start
                    last = log.last
                    idx = log.last
                    eventIterator = updateIterator()
                }

                private fun updateIterator(): Iterator<LogEvent>? {
                    idx = Math.max(idx - ITERATION_STEP, first)
                    eventIterator = readLogRecords(idx, Math.min(idx + ITERATION_STEP - 1, last)).reversed().iterator()
                    return eventIterator
                }

                override fun next(): LogEvent {
                    if (eventIterator != null) {
                        if (eventIterator!!.hasNext()) {
                            return eventIterator!!.next()
                        } else {
                            updateIterator()
                            if (eventIterator != null)
                                return eventIterator!!.next()
                        }
                    }
                    throw UnsupportedOperationException()
                }

                override fun hasNext(): Boolean {
                    if (eventIterator?.hasNext() == true)
                        return true
                    else {
                        if (idx > first)
                            updateIterator()
                        else
                            eventIterator = null
                        return eventIterator != null
                    }
                }
            }
        }
    }

    private data class BasalRecordHolder (val tempBasalStart: TempRateStart? = null,
                                          val tempRateCompleted: TempRateCompleted? = null,
                                          val basalRateChange: BasalRateChange? = null,
                                          val pumpingSuspended: PumpingSuspended? = null,
                                          val pumpingResumed: PumpingResumed? = null) {
        fun scheduled() : TandemScheduledBasalRecord? {
            return if (basalRateChange != null)
                TandemScheduledBasalRecord(rateChange = basalRateChange, schedule = null)
            else null
        }

        fun temp() : TandemTemporaryBasalRecord? {
            val result = if (tempBasalStart != null)
                TandemTemporaryBasalRecord(tempBasalStart, tempRateCompleted, basalRateChange)
            else if (pumpingSuspended != null && pumpingResumed != null)
                TandemTemporaryBasalRecord(pumpingSuspended, pumpingResumed, basalRateChange)
            else {
                null
            }
            return result
        }

    }

    private inner class ProfileAssemblingSequence : Sequence<ProfileRecord> {
        override fun iterator(): Iterator<ProfileRecord> = object: Iterator<ProfileRecord> {
            var recordRetrieved = false
            val recordIterator: Iterator<TandemTherapyRecord>

            override fun hasNext(): Boolean {
                return !recordRetrieved && recordIterator.hasNext()
            }

            override fun next(): ProfileRecord {
                recordRetrieved = true
                val profileMap = profiles.map { entry -> entry.name to entry }.toMap()
                val profileList = recordIterator.next()
                return TandemProfileRecord(profiles[0].name, profileMap, profileList)
            }

            init {
                val seq = records.filter {
                    when (it) {
                        is IdpRecord -> true
                        is IdpList -> true
                        else -> false
                    }
                }.map { it as TandemTherapyRecord }
                recordIterator = seq.iterator()
            }
        }
    }

    private inner class BasalRateAssemblingSequence : Sequence<BasalRecord> {

        override fun iterator() = object: Iterator<BasalRecord> {
            var basalRecordHolder = BasalRecordHolder()
            val recordIterator: Iterator<BasalRecord>

            init {
                recordIterator = records.filterIsInstance<BasalRecord>()
                        .flatMap {
                            when (it) {
                                is BasalRateChange -> {
                                    basalRecordHolder = basalRecordHolder.copy(basalRateChange = it)
                                    if (it.changeType and (BasalRateChange.MASK_PROFILE_CHANGE or
                                            BasalRateChange.MASK_SEGMENT_CHANGE or
                                            BasalRateChange.MASK_PUMP_RESUME or
                                            BasalRateChange.MASK_TEMP_END) != 0) {
                                        val v = basalRecordHolder.scheduled()
                                        if (v != null)
                                            sequenceOf<BasalRecord>(v)
                                        else
                                            emptySequence()
                                    } else
                                        emptySequence()
                                }
                                is TempRateCompleted -> {
                                    basalRecordHolder = BasalRecordHolder(tempRateCompleted = it)
                                    sequenceOf(it)
                                }
                                is PumpingResumed -> {
                                    basalRecordHolder = BasalRecordHolder(pumpingResumed = it)
                                    sequenceOf(it)
                                }
                                is TempRateStart -> {
                                    val v = basalRecordHolder.copy(tempBasalStart = it).temp()
                                    basalRecordHolder = BasalRecordHolder()
                                    if (v != null)
                                        sequenceOf<BasalRecord>(v, it)
                                    else
                                        emptySequence()
                                }
                                is PumpingSuspended -> {
                                    val v = basalRecordHolder.copy(pumpingSuspended = it).temp()
                                    basalRecordHolder = BasalRecordHolder()
                                    if (v != null)
                                        sequenceOf<BasalRecord>(v, it)
                                    else
                                        emptySequence()
                                }
                                else -> emptySequence<BasalRecord>()
                            }
                        }.iterator()
            }

            override fun next(): BasalRecord {
                return recordIterator.next()
            }

            override fun hasNext(): Boolean {
                return recordIterator.hasNext()
            }
        }
    }

    private data class BolusRecordHolder (var record1: BolusRequest1? = null,
                                          var record2: BolusRequest2? = null,
                                          var record3: BolusRequest3? = null,
                                          var normalActivated: BolusActivated? = null,
                                          var extendedActivated: BolexActivated? = null,
                                          var normalCompleted: BolusCompleted? = null,
                                          var extendedCompleted: BolexCompleted? = null) {

        @Suppress("USELESS_CAST") // the compiler can't figure it out without a little help.
        fun toRecord(): TandemBolus? {
            val wizard = if (record1 != null && record2 != null && record3 != null)
                TandemBolusWizard(record1!!, record2!!, record3!!)
            else null
            return if (wizard != null) {
                if (normalActivated != null && extendedActivated != null)
                    TandemComboBolus(normalActivated!!, extendedActivated!!, normalCompleted, extendedCompleted, wizard)
                else if (normalActivated != null)
                    TandemNormalBolus(normalActivated!!, normalCompleted, wizard)
                else if (extendedActivated != null)
                    TandemExtendedBolus(extendedActivated!!, extendedCompleted, wizard) as TandemBolus
                else null
            } else null
        }
    }

    private inner class BolusWizardAssemblingSequence : Sequence<TandemBolus> {
        override fun iterator() = object: Iterator<TandemBolus> {
            val bolusRecordMap = LinkedHashMap<Int, BolusRecordHolder>()
            val recordIterator: Iterator<TandemBolus>

            init {
                recordIterator = records.filterIsInstance<BolusEventRecord>()
                        .mapNotNull {
                            val recordHolder = bolusRecordMap.getOrPut(it.bolusId, { BolusRecordHolder() })
                            when (it) {
                                is BolusRequest1 -> {
                                    recordHolder.record1 = it
                                    bolusRecordMap.remove(it.bolusId)
                                    recordHolder.toRecord()
                                }
                                is BolusRequest2 -> {
                                    recordHolder.record2 = it
                                    null
                                }
                                is BolusRequest3 -> {
                                    recordHolder.record3 = it
                                    null
                                }
                                is BolusActivated -> {
                                    recordHolder.normalActivated = it
                                    null
                                }
                                is BolexActivated -> {
                                    recordHolder.extendedActivated = it
                                    null
                                }
                                is BolusCompleted -> {
                                    recordHolder.normalCompleted = it
                                    null
                                }
                                is BolexCompleted -> {
                                    recordHolder.extendedCompleted = it
                                    null
                                }
                                else -> null
                            }
                        }.iterator()
            }

            override fun next(): TandemBolus {
                return recordIterator.next()
            }

            override fun hasNext(): Boolean {
                return recordIterator.hasNext()
            }
        }
    }
}

operator fun MutableMap<Int,TandemProfile>.invoke(idpBolus: IdpBolus) {
    println("idpbolus: $idpBolus")
    println("profiles: $this")

    val profile = this[idpBolus.idp]!!
    this += idpBolus.idp to profile.copy(dia = idpBolus.insulinDuration ?: profile.dia)
}

operator fun MutableMap<Int,TandemProfile>.invoke(idp: Idp) {
    println("idp: $idp")
    println("profiles: $this")
    when (idp.op) {
        Idp.OP_COPY -> {
            this += idp.idp to this[idp.sourceIdp]!!.copy(idp = idp.idp,
                    name = idp.name.trim(0.toChar()))
        }
        Idp.OP_DELETE -> {
            this.remove(idp.idp)
        }
        Idp.OP_NEW -> {
            this += idp.idp to TandemProfile(idp = idp.idp, name = idp.name)
        }
        Idp.OP_RENAME -> {
            this += idp.idp to this[idp.idp]!!.copy(name = idp.name)
        }
        Idp.OP_SELECT -> {
            // handle profile activation
        }
    }
}

operator fun MutableMap<Int, TandemProfile>.invoke(idpList: IdpList) {
    println("idplist: $idpList")
    println("profiles: $this")
    val profiles = idpList.slots.take(idpList.numProfiles)
    val iterator = this.iterator()
    for (profile in iterator) {
        if (profile.key !in profiles)
            iterator.remove()
    }
}

operator fun MutableMap<Int, TandemProfile>.invoke(idpMessage2: IdpMessage2) {
    println("idpMessage2: $idpMessage2")
    println("profiles: $this")
    val profile = this[idpMessage2.idp]!!
    this += idpMessage2.idp to profile.copy(name = "${profile.name}${idpMessage2.nameCont}")
}

operator fun MutableMap<Int, TandemProfile>.invoke(idpTdSegment: IdpTdSeg) {
    println("idpTdSegment: $idpTdSegment")
    println("profiles: $this")
    val profile = this[idpTdSegment.idp]!!
    //this += idpTdSegment.idp to profile.copy(name = "${profile.name}${idpTdSegment.nameCont}")
}

