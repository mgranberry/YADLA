package com.kludgenics.justgivemeachart.interpolators

import android.util.Log
import com.kludgenics.justgivemeachart.ValueAdapter

/**
 * Created by matthias on 4/26/16.
 */
class StepInterpolator(var values: List<ValueAdapter.Point<Float, Float>?>) : Interpolator {
    val nonNulls = values.filterNotNull()
    override fun interpolate(index: Float): ValueAdapter.Point<Float, Float> {
        val reference = nonNulls[nonNulls.binarySearchBy(index) { it.index }]
        return ValueAdapter.Point(reference.value, index)
    }
}
