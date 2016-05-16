package com.kludgenics.justgivemeachart

/**
 * Created by matthias on 4/26/16.
 */
open class StackedLine<T, U>(private val base: Line<T, U>, private val line: Line<T, U>): Line<T, U> by line {
    override fun baselineValueAt(index: Float): Float =
        base.baselineValueAt(index) + line.baselineValueAt(index)
}