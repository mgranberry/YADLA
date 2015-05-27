package com.kludgenics.cgmlogger.data.logdata.glucose.data

import java.util.Date

/**
 * Created by matthiasgranberry on 5/12/15.
 */
public trait BloodGlucose {
    public fun getId(): String
    public fun getValue(): Double
    public fun getDate(): Date
    public fun getType(): String
    public fun getUnit(): String

    companion object {
        public val UNIT_MGDL: String = "mg/dl"
        public val UNIT_MMOL: String = "mmol/l"
        public val TYPE_CGM: String = "cgm"
        public val TYPE_SMBG: String = "smbg"
    }
}

/** Implemented as an extension to facilitate interaction with Realm objects
 * @return mg/dL
 */
fun BloodGlucose.asMgDl(): Int {
    return when (getUnit()) {
        in BloodGlucose.UNIT_MGDL -> getValue().toInt()
        in BloodGlucose.UNIT_MMOL -> (getValue() * 18.01559).toInt()
        else -> getValue().toInt()
    }
}


/** Implemented as an extension to facilitate interaction with Realm objects
 * @return mmol/L
 */
fun BloodGlucose.asMmol(): Double {
    return when (getUnit()) {
        in BloodGlucose.UNIT_MGDL -> getValue() / 18.01559
        in BloodGlucose.UNIT_MMOL -> getValue()
        else -> getValue()
    }
}

/** Implemented as an extension to facilitate interaction with Realm objects
 * @return unitized BG
 */
fun BloodGlucose.asUnit(unit: String): Double {
    return when (unit) {
        in BloodGlucose.UNIT_MGDL -> asMgDl().toDouble()
        in BloodGlucose.UNIT_MMOL -> asMmol()
        else -> getValue()
    }
}