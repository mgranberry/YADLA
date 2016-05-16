package com.kludgenics.justgivemeachart

/**
 * Created by matthias on 4/22/16.
 */
interface Line<T, U>: DataInk<T, U> {
    fun baselineValueAt(index: Float): Float
    fun invalidate()
}