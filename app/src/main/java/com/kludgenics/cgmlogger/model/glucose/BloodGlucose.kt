package com.kludgenics.cgmlogger.model.glucose

/**
 * Created by matthiasgranberry on 5/12/15.
 */
public interface BloodGlucose {
    val unit: String
    val value: Double
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
    return when (unit) {
        BloodGlucose.UNIT_MGDL -> value.toInt()
        BloodGlucose.UNIT_MMOL -> (value * 18.01559).toInt()
        else -> throw IllegalArgumentException("Invalid unit ${unit}")
    }
}


/** Implemented as an extension to facilitate interaction with Realm objects
 * @return mmol/L
 */
fun BloodGlucose.asMmol(): Double {
    return when (unit) {
        BloodGlucose.UNIT_MGDL -> value / 18.01559
        BloodGlucose.UNIT_MMOL -> value.toDouble()
        else -> throw IllegalArgumentException("Invalid unit ${unit}")
    }
}

/** Implemented as an extension to facilitate interaction with Realm objects
 * @return unitized BG
 */
fun BloodGlucose.asUnit(unit: String): Double {
    return when (unit) {
        BloodGlucose.UNIT_MGDL -> asMgDl().toDouble()
        BloodGlucose.UNIT_MMOL -> asMmol()
        else -> throw IllegalArgumentException("Invalid unit $unit")
    }
}
