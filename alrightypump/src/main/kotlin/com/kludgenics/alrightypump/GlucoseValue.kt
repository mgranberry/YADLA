package com.kludgenics.alrightypump

import sun.plugin.dom.exception.InvalidStateException

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
    val glucose: Double
    val mgdl: Double get() = if (unit == GlucoseUnit.MGDL)
        glucose
    else if (unit == GlucoseUnit.MMOL)
        glucose * GlucoseUnit.MMOL_MGDL_FACTOR
    else throw InvalidStateException("unsupported glucose unit: ${unit}")

    val mmol: Double get() = if (unit == GlucoseUnit.MGDL)
        glucose / GlucoseUnit.MMOL_MGDL_FACTOR
    else if (unit == GlucoseUnit.MMOL)
        glucose
    else throw InvalidStateException("unsupported glucose unit: ${unit}")

    val unit: Int

}