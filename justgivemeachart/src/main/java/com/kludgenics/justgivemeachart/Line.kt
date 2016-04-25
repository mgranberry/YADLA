package com.kludgenics.justgivemeachart

/**
 * Created by matthias on 4/22/16.
 */
interface Line: Ink {
    fun baselineValueAt(x: Float): Float
}