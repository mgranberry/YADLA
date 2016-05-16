package com.kludgenics.justgivemeachart.interpolators

import com.kludgenics.justgivemeachart.ValueAdapter

/**
 * Created by matthias on 4/22/16.
 */
interface Interpolator {
    fun interpolate(index: Float): ValueAdapter.Point<Float, Float>
}