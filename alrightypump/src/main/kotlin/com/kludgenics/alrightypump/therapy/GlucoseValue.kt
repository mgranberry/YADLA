package com.kludgenics.alrightypump.therapy

/**
 * Created by matthias on 11/7/15.
 */

interface GlucoseUnit {
    companion object {
        const val MMOL = 0
        const val MGDL = 1
        const val MMOL_MGDL_FACTOR = 18.01559
    }
}

interface GlucoseValue {
    val glucose: Double?
    val mgdl: Double? get() = if (unit == GlucoseUnit.MGDL)
        glucose
    else if (unit == GlucoseUnit.MMOL)
        glucose?.times(GlucoseUnit.MMOL_MGDL_FACTOR)
    else null

    val mmol: Double? get() = if (unit == GlucoseUnit.MGDL)
        glucose?.div(GlucoseUnit.MMOL_MGDL_FACTOR)
    else if (unit == GlucoseUnit.MMOL)
        glucose
    else null

    val unit: Int
}

open class BaseGlucoseValue(override val glucose: Double,
                                   override val unit: Int) : GlucoseValue {
    override fun toString(): String {
        return "${this.javaClass.simpleName}(glucose=$glucose, unit=$unit)"
    }
}

interface Calibration {
    val slope: Double
    val intercept: Double
    val scale: Double
    val decay: Double
    fun apply(bgl: RawGlucoseValue): Double? {
        val mgdl = bgl.mgdl
        val filtered = bgl.filtered
        val unfiltered = bgl.unfiltered
        return if (unfiltered != null) {
            if ((mgdl != null) && (mgdl >= 40.0) && (mgdl <= 400.0) && filtered != null) {
                val ratio = scale * (filtered - intercept) / slope / mgdl
                scale * (unfiltered - intercept) / slope / ratio
            } else
                scale * (unfiltered - intercept) / slope
        } else
            null
    }
}

interface RawGlucoseValue : GlucoseValue {
    val raw: Double? get() = calibration?.apply(this)
    val rawMgdl: Double? get() = raw
    val rawMmol: Double? get() = raw?.div(GlucoseUnit.MMOL_MGDL_FACTOR)
    val calibration: Calibration?
    val filtered: Int?
    val unfiltered: Int?
}