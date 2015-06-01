package com.kludgenics.cgmlogger.data.glucose

import org.jetbrains.annotations.NotNull
import java.util.Date
import java.util.InvalidPropertiesFormatException
import kotlin.properties.Delegates

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
        BloodGlucose.UNIT_MGDL -> getValue().toInt()
        BloodGlucose.UNIT_MMOL -> (getValue() * 18.01559).toInt()
        else -> throw IllegalArgumentException("Invalid unit ${getUnit()}")
    }
}


/** Implemented as an extension to facilitate interaction with Realm objects
 * @return mmol/L
 */
fun BloodGlucose.asMmol(): Double {
    return when (getUnit()) {
        BloodGlucose.UNIT_MGDL -> getValue() / 18.01559
        BloodGlucose.UNIT_MMOL -> getValue()
        else -> throw IllegalArgumentException("Invalid unit ${getUnit()}")
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
