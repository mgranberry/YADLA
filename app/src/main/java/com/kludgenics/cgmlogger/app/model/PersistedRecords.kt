package com.kludgenics.cgmlogger.app.model

import com.crashlytics.android.Crashlytics
import com.kludgenics.alrightypump.therapy.*
import com.kludgenics.cgmlogger.extension.transaction
import com.kludgenics.cgmlogger.extension.where
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import java.util.*

interface TypedRecord : Record {
    companion object {
        val CLASSES = arrayOf<Class<out RealmObject>>(PersistedRawCgmRecord::class.java,
                PersistedCgmRecord::class.java,
                PersistedBolusRecord::class.java,
                PersistedBolusWizardRecord::class.java,
                PersistedCalibrationRecord::class.java,
                PersistedTemporaryBasalStartRecord::class.java,
                PersistedTemporaryBasalEndRecord::class.java,
                PersistedSuspendedBasalRecord::class.java,
                PersistedSmbgRecord::class.java,
                PersistedCannulaChangedRecord::class.java,
                PersistedCartridgeChangeRecord::class.java,
                PersistedCgmInsertionRecord::class.java,
                PersistedFoodRecord::class.java)

        fun inflate(record: TypedRecord): TypedRecord? {
            val realm = Realm.getDefaultInstance()
            val realmClass =
                    try {
                        CLASSES.first { realm.where(it).equalTo("record.eventKey", record.eventKey).findAll().size != 0 }
                    } catch (e: java.util.NoSuchElementException) {
                        println("Boo! element for $record could not be found ")
                        Crashlytics.log("record is: $record")
                        Crashlytics.logException(e)
                        null
                    }
            return if (realmClass != null) realm.where(realmClass).equalTo("record.eventKey", record.eventKey).findFirst() as TypedRecord else null
        }
    }

    var _date: Date
    var eventKey: String
    val eventType: Int
}

open class PersistedRecord(var syncedStores: RealmList<SyncStore> = RealmList(),
                           var _id: String? = null,
                           override var _date: Date = Date(0),
                           var _source: String = "",
                           var _eventType: Int = EventType.INVALID) : RealmObject(), TypedRecord {
    override val id: String? get() = _id
    override val source: String get() = _source
    override val time: LocalDateTime get() = LocalDateTime(_date.time)
    override val eventType: Int get()  { return _eventType }
    @PrimaryKey
    override var eventKey: String = ""
}

object EventType {
    const val BOLUS = 0
    const val BASAL = 1
    const val GLUCOSE = 2
    const val OTHER = 3
    const val INVALID = 4
}

class PersistedTherapyTimeline() : TherapyTimeline {
    private val realm: Realm get() = Realm.getDefaultInstance()

    override val events: Sequence<Record>
        get() = realm.where<PersistedRecord>().findAllSorted("_date").asSequence().map { TypedRecord.inflate(it) }.filterNotNull()

    override val bolusEvents: Sequence<BolusRecord>
        get() = realm.where<PersistedRecord> {
            equalTo("_eventType", EventType.BOLUS)
        }.findAllSorted("_date").asSequence().map { TypedRecord.inflate(it) as BolusRecord? }.filterNotNull()

    override val glucoseEvents: Sequence<GlucoseRecord>
        get() = realm.where<PersistedRecord> {
            equalTo("_eventType", EventType.GLUCOSE)
        }.findAllSorted("_date").asSequence().map { TypedRecord.inflate(it) as GlucoseRecord? }.filterNotNull()

    override val basalEvents: Sequence<BasalRecord>
        get() = realm.where<PersistedRecord> {
            equalTo("_eventType", EventType.BASAL)
        }.findAllSorted("_date").asSequence().map { TypedRecord.inflate(it) as BasalRecord? }.filterNotNull()

    override fun events(start: LocalDateTime, end: LocalDateTime): Collection<Record> {
        return realm.where<PersistedRecord> {
            between("_date", start.toDate(), end.toDate())
        }.findAllSorted("_date").asSequence().map { TypedRecord.inflate(it) }.filterNotNull().toList()
    }

    override fun merge(vararg additionalEvents: Sequence<Record>) {
        merge({ true }, *additionalEvents)
    }

    override fun merge(predicate: (Record) -> Boolean, vararg additionalEvents: Sequence<Record>) {
        val events = additionalEvents.flatMap { it.takeWhile(predicate).asIterable() }
        events.forEach { event ->
            val record = PersistedRecord(_id = event.id, _date = Date(event.time.toDateTime().millis), _source = event.source)
            record.eventKey = "${event.javaClass.simpleName}-${event.time.toDate().time}"
            val persistedRecord: RealmObject? = when (event) {
                is CalibrationRecord -> PersistedCalibrationRecord(record, event as Calibration)
                is RawCgmRecord -> PersistedRawCgmRecord(record, event)
                is CgmRecord -> PersistedCgmRecord(record, event)
                is SmbgRecord -> PersistedSmbgRecord(record, event)
                is FoodRecord -> PersistedFoodRecord(record, event)
                is CgmInsertionRecord -> PersistedCgmInsertionRecord(record, event)
                is TemporaryBasalStartRecord -> PersistedTemporaryBasalStartRecord(record, event)
                is TemporaryBasalEndRecord -> PersistedTemporaryBasalEndRecord(record, event)
                is SuspendedBasalRecord -> PersistedSuspendedBasalRecord(record, event)
                is CannulaChangedRecord -> PersistedCannulaChangedRecord(record)
                is CartridgeChangeRecord -> PersistedCartridgeChangeRecord(record)
            //is ScheduledBasalRecord,
                is BolusRecord -> PersistedBolusRecord(record, event)
                else -> null
            }
            if (persistedRecord != null && persistedRecord is TypedRecord) {
                record._eventType = persistedRecord.eventType
                persistedRecord.eventKey = record.eventKey
                realm.transaction {
                    realm.copyToRealmOrUpdate(persistedRecord) as TypedRecord
                }
            }
        }
    }
}


open class PersistedCalibration(var _slope: Double = Double.NaN,
                                var _intercept: Double = Double.NaN,
                                var _scale: Double = Double.NaN,
                                var _decay: Double = Double.NaN) : Calibration, RealmObject() {
    constructor(calibration: Calibration) : this(_slope = calibration.slope,
            _intercept = calibration.intercept,
            _scale = calibration.scale,
            _decay = calibration.decay)

    override val slope: Double get() = _slope
    override val intercept: Double get() = _intercept
    override val scale: Double get() = _scale
    override val decay: Double get() = _decay
}

open class PersistedCalibrationRecord(var record: PersistedRecord = PersistedRecord(),
                                      var _calibration: PersistedCalibration = PersistedCalibration(),
                                      @PrimaryKey
                                      override var eventKey: String="") : Calibration by _calibration, TypedRecord, RealmObject() {
    override val eventType: Int
        get() = EventType.OTHER

    constructor(record: PersistedRecord, calibration: Calibration) : this(record, PersistedCalibration(calibration))

    override var _date: Date
        get() = record._date
        set(value) {
            record._date = value
        }
    override val id: String?
        get() = record._id
    override val time: LocalDateTime
        get() = record.time
    override val source: String
        get() = record._source
}

open class PersistedGlucoseValue(var _glucose: Double? = null, var _unit: Int = GlucoseUnit.MGDL) : GlucoseValue, RealmObject() {
    constructor(glucoseValue: GlucoseValue) : this(glucoseValue.glucose, glucoseValue.unit)

    override val glucose: Double? get() = _glucose
    override val unit: Int get() = _unit
}

open class PersistedRawGlucoseValue(var _glucose: Double? = null,
                                    var _unit: Int = GlucoseUnit.MGDL,
                                    var _calibration: PersistedCalibration? = null,
                                    var _filtered: Int? = null,
                                    var _unfiltered: Int? = null) : RawGlucoseValue, RealmObject() {
    constructor(rawGlucoseValue: RawGlucoseValue) : this(_glucose = rawGlucoseValue.glucose,
            _unit = rawGlucoseValue.unit,
            _calibration = null)

    override val glucose: Double? get() = _glucose
    override val unit: Int get() = _unit
    override val calibration: Calibration? get() = _calibration
    override val filtered: Int? get() = _filtered
    override val unfiltered: Int? get() = _unfiltered
}

open class PersistedRawCgmRecord(var record: PersistedRecord = PersistedRecord(),
                                 var _value: PersistedRawGlucoseValue = PersistedRawGlucoseValue(),
                                 @PrimaryKey
                                 override var eventKey: String="") : TypedRecord, RawCgmRecord, RealmObject() {
    constructor (record: PersistedRecord, rawCgmRecord: RawCgmRecord) : this(record, PersistedRawGlucoseValue(rawCgmRecord.value))
    override val value: PersistedRawGlucoseValue get() = _value
    override val eventType: Int
        get() = EventType.GLUCOSE

    override var _date: Date
        get() = record._date
        set(value) {
            record._date = value
        }
    override val id: String?
        get() = record._id
    override val time: LocalDateTime
        get() = record.time
    override val source: String
        get() = record._source
}

open class PersistedCgmRecord(var record: PersistedRecord = PersistedRecord(),
                              var _value: PersistedGlucoseValue = PersistedGlucoseValue(),
                              @PrimaryKey
                              override var eventKey: String="") : TypedRecord, CgmRecord, RealmObject() {
    constructor (record: PersistedRecord, rawCgmRecord: CgmRecord) : this(record, PersistedGlucoseValue(rawCgmRecord.value))
    override var _date: Date
        get() = record._date
        set(value) {
            record._date = value
        }
    override val id: String?
        get() = record._id
    override val time: LocalDateTime
        get() = record.time
    override val source: String
        get() = record._source
    override val value: GlucoseValue get() = _value
    override val eventType: Int
        get() = EventType.GLUCOSE
}

open class PersistedSmbgRecord(var record: PersistedRecord = PersistedRecord(),
                               var _value: PersistedGlucoseValue = PersistedGlucoseValue(),
                               var _manual: Boolean = true,
                               @PrimaryKey
                               override var eventKey: String="") : TypedRecord, SmbgRecord, RealmObject() {
    constructor (record: PersistedRecord, smbgRecord: SmbgRecord) : this(record, PersistedGlucoseValue(smbgRecord.value), smbgRecord.manual)
    override var _date: Date
        get() = record._date
        set(value) {
            record._date = value
        }
    override val id: String?
        get() = record._id
    override val time: LocalDateTime
        get() = record.time
    override val source: String
        get() = record._source
    override val value: GlucoseValue
        get() = _value
    override val manual: Boolean
        get() = _manual
    override val eventType: Int
        get() = EventType.GLUCOSE
}

open class PersistedFoodRecord(var record: PersistedRecord = PersistedRecord(),
                               var _carbohydrateGrams: Int = 0,
                               @PrimaryKey
                               override var eventKey: String="") : TypedRecord, FoodRecord, RealmObject() {
    constructor(record: PersistedRecord, foodRecord: FoodRecord) : this (record, _carbohydrateGrams = foodRecord.carbohydrateGrams)
    override var _date: Date
        get() = record._date
        set(value) {
            record._date = value
        }
    override val id: String?
        get() = record._id
    override val time: LocalDateTime
        get() = record.time
    override val source: String
        get() = record._source

    override val carbohydrateGrams: Int get() = _carbohydrateGrams
    override val eventType: Int
        get() = EventType.OTHER
}

open class PersistedCgmInsertionRecord(var record: PersistedRecord = PersistedRecord(),
                                       var _removed: Boolean = false,
                                       @PrimaryKey
                                       override var eventKey: String="") : TypedRecord, CgmInsertionRecord, RealmObject() {
    constructor(record: PersistedRecord, cgmInsertionRecord: CgmInsertionRecord) : this (record, _removed = cgmInsertionRecord.removed)
    override var _date: Date
        get() = record._date
        set(value) {
            record._date = value
        }
    override val id: String?
        get() = record._id
    override val time: LocalDateTime
        get() = record.time
    override val source: String
        get() = record._source

    override val removed: Boolean
        get() = _removed
    override val eventType: Int
        get() = EventType.OTHER
}

interface PersistedBasalRecord : TypedRecord, BasalRecord {
    var _rate: Double?
    override val rate: Double?
        get() = _rate
    override val eventType: Int
        get() = EventType.BASAL
}

interface PersistedTemporaryBasalRecord : PersistedBasalRecord, TemporaryBasalRecord {

    var _percent: Double?
    override val percent: Double?
        get() = _percent

    var _durationMillis: Long
    override val duration: Duration
        get() = Duration(_durationMillis)
    override val eventType: Int
        get() = EventType.BASAL
}

open class PersistedTemporaryBasalStartRecord(var record: PersistedRecord = PersistedRecord(),
                                              override var _percent: Double? = null,
                                              override var _durationMillis: Long = 0,
                                              override var _rate: Double? = null,
                                              @PrimaryKey
                                              override var eventKey: String="") : TypedRecord, PersistedTemporaryBasalRecord, TemporaryBasalStartRecord, RealmObject() {
    constructor(record: PersistedRecord, temporaryBasalStartRecord: TemporaryBasalStartRecord) : this (record, _percent = temporaryBasalStartRecord.percent,
            _durationMillis = temporaryBasalStartRecord.duration.millis,
            _rate = temporaryBasalStartRecord.rate)
    override var _date: Date
        get() = record._date
        set(value) {
            record._date = value
        }
    override val id: String?
        get() = record._id
    override val time: LocalDateTime
        get() = record.time
    override val source: String
        get() = record._source
}

open class PersistedTemporaryBasalEndRecord(var record: PersistedRecord = PersistedRecord(),
                                            override var _percent: Double? = null,
                                            override var _durationMillis: Long = 0,
                                            override var _rate: Double? = null,
                                            @PrimaryKey
                                            override var eventKey: String="") : TypedRecord, PersistedTemporaryBasalRecord, TemporaryBasalEndRecord, RealmObject() {
    constructor(record: PersistedRecord, temporaryBasalEndRecord: TemporaryBasalEndRecord) : this (record, _percent = temporaryBasalEndRecord.percent,
            _durationMillis = temporaryBasalEndRecord.duration.millis,
            _rate = temporaryBasalEndRecord.rate)
    override var _date: Date
        get() = record._date
        set(value) {
            record._date = value
        }
    override val id: String?
        get() = record._id
    override val time: LocalDateTime
        get() = record.time
    override val source: String
        get() = record._source

    override val duration: Duration
        get() = super<PersistedTemporaryBasalRecord>.duration
    override val rate: Double?
        get() = super<PersistedTemporaryBasalRecord>.rate
}

open class PersistedSuspendedBasalRecord(var record: PersistedRecord = PersistedRecord(),
                                         override var _percent: Double? = null,
                                         override var _durationMillis: Long = 0,
                                         override var _rate: Double? = null,
                                         @PrimaryKey
                                         override var eventKey: String="") : TypedRecord, PersistedTemporaryBasalRecord, SuspendedBasalRecord, RealmObject() {
    constructor(record: PersistedRecord, suspendedBasalRecord: SuspendedBasalRecord) : this (record, _percent = suspendedBasalRecord.percent,
            _durationMillis = suspendedBasalRecord.duration.millis,
            _rate = suspendedBasalRecord.rate)
    override var _date: Date
        get() = record._date
        set(value) {
            record._date = value
        }
    override val id: String?
        get() = record._id
    override val time: LocalDateTime
        get() = record.time
    override val source: String
        get() = record._source
}

open class PersistedCannulaChangedRecord(var record: PersistedRecord = PersistedRecord(),
                                         @PrimaryKey
                                         override var eventKey: String="") : TypedRecord, CannulaChangedRecord, RealmObject() {
    override var _date: Date
        get() = record._date
        set(value) {
            record._date = value
        }
    override val id: String?
        get() = record._id
    override val time: LocalDateTime
        get() = record.time
    override val source: String
        get() = record._source
    override val eventType: Int
        get() = EventType.OTHER
}

open class PersistedCartridgeChangeRecord(var record: PersistedRecord = PersistedRecord(),
                                          @PrimaryKey
                                          override var eventKey: String="") : TypedRecord, CartridgeChangeRecord, RealmObject() {
    override var _date: Date
        get() = record._date
        set(value) {
            record._date = value
        }
    override val id: String?
        get() = record._id
    override val time: LocalDateTime
        get() = record.time
    override val source: String
        get() = record._source
    override val eventType: Int
        get() = EventType.OTHER
}

open class PersistedBloodGlucoseTarget(var _targetLow: PersistedGlucoseValue = PersistedGlucoseValue(),
                                       var _targetHigh: PersistedGlucoseValue = PersistedGlucoseValue()) : BloodGlucoseTarget, RealmObject() {
    override val targetLow: GlucoseValue
        get() = _targetLow
    override val targetHigh: GlucoseValue
        get() = _targetHigh
}

open class PersistedBolusWizardRecommendation(var _carbBolus: Double = Double.NaN,
                                              var _correctionBolus: Double = Double.NaN) : BolusWizardRecord.Recommendation, RealmObject() {
    override val carbBolus: Double
        get() = _carbBolus
    override val correctionBolus: Double
        get() = _correctionBolus
}

open class PersistedBolusWizardRecord(var record: PersistedRecord = PersistedRecord(),
                                      var _bg: PersistedGlucoseValue = PersistedGlucoseValue(),
                                      var _carbs: Int = 0,
                                      var _insulinOnBoard: Double = Double.NaN,
                                      var _carbRatio: Double = Double.NaN,
                                      var _insulinSensitivity: Double = Double.NaN,
                                      var _target: PersistedBloodGlucoseTarget = PersistedBloodGlucoseTarget(),
                                      var _recommendation: PersistedBolusWizardRecommendation = PersistedBolusWizardRecommendation(),
                                      override var eventKey: String = "") : TypedRecord, BolusWizardRecord, RealmObject() {


    constructor(bolusWizardRecord: BolusWizardRecord) : this(record=PersistedRecord(),
            _bg = PersistedGlucoseValue(bolusWizardRecord.bg),
            _carbs = bolusWizardRecord.carbs,
            _insulinOnBoard = bolusWizardRecord.insulinOnBoard,
            _carbRatio = bolusWizardRecord.carbRatio,
            _insulinSensitivity = bolusWizardRecord.insulinSensitivity,
            _target = with (bolusWizardRecord.target) { PersistedBloodGlucoseTarget(PersistedGlucoseValue(targetLow), PersistedGlucoseValue(targetHigh)) },
            _recommendation = with (bolusWizardRecord.recommendation) { PersistedBolusWizardRecommendation(
                    _carbBolus = carbBolus,
                    _correctionBolus = correctionBolus)})
    override var _date: Date
        get() = record._date
        set(value) {
            record._date = value
        }
    override val id: String?
        get() = record._id
    override val time: LocalDateTime
        get() = record.time
    override val source: String
        get() = record._source
    override val eventType: Int
        get() = EventType.OTHER
    override val bg: GlucoseValue
        get() = _bg
    override val carbs: Int
        get() = _carbs
    override val insulinOnBoard: Double
        get() = _insulinOnBoard
    override val carbRatio: Double
        get() = _carbRatio
    override val insulinSensitivity: Double
        get() = _insulinSensitivity
    override val target: BloodGlucoseTarget
        get() = _target
    override val recommendation: BolusWizardRecord.Recommendation
        get() = _recommendation
}

open class PersistedBolusRecord(var record: PersistedRecord = PersistedRecord(),
                                var _requestedNormal: Double? = null,
                                var _deliveredNormal: Double? = null,
                                var _requestedExtended: Double? = null,
                                var _deliveredExtended: Double? = null,
                                var _extendedDuration: Long? = null,
                                var _expectedExtendedDuration: Long? = null,
                                var _bolusWizard: PersistedBolusWizardRecord? = null,
                                var _manual: Boolean = false,
                                @PrimaryKey
                                override var eventKey: String="") : TypedRecord, BolusRecord, RealmObject() {
    override var _date: Date
        get() = record._date
        set(value) {
            record._date = value
        }
    override val id: String?
        get() = record._id
    override val time: LocalDateTime
        get() = record.time
    override val source: String
        get() = record._source

    constructor(record: PersistedRecord, bolusRecord: BolusRecord) : this (record,
            _requestedNormal = bolusRecord.requestedNormal,
            _deliveredNormal = bolusRecord.deliveredNormal,
            _requestedExtended = bolusRecord.requestedExtended,
            _deliveredExtended = bolusRecord.deliveredExtended,
            _extendedDuration = bolusRecord.extendedDuration?.millis,
            _bolusWizard = if (bolusRecord.bolusWizard != null) PersistedBolusWizardRecord(bolusRecord.bolusWizard!!) else null,
            _manual = bolusRecord.manual)
    override val eventType: Int
        get() = EventType.BOLUS
    override val requestedNormal: Double?
        get() = _requestedNormal
    override val deliveredNormal: Double?
        get() = _deliveredNormal
    override val requestedExtended: Double?
        get() = _requestedExtended
    override val deliveredExtended: Double?
        get() = _deliveredExtended
    override val extendedDuration: Duration?
        get() {
            val duration = _extendedDuration
            return if (duration != null)
                Duration(duration)
            else
                null
        }
    override val expectedExtendedDuration: Duration?
        get() {
            val duration = _expectedExtendedDuration
            return if (duration != null)
                Duration(duration)
            else
                null
        }
    override val bolusWizard: BolusWizardRecord?
        get() = _bolusWizard
    override val manual: Boolean
        get() = _manual
}
