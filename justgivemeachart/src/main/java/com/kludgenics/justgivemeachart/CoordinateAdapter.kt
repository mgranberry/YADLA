package com.kludgenics.justgivemeachart

/**
 * Transforms native data coordinates into a [0, 1] range used internally for the graph.
 */
interface CoordinateAdapter<T> {

    /**
     * Set the maximum value to display on the graph.
     */
    fun<T>setMaximumValue(value: T)

    /**
     * Set the minimum value to display on the graph.
     */
    fun<T>setMinumumValue(value: T)

    /**
     * Returns a scaled coordinate for [value] given current min/max values.
     * @return the scaled coordinate for [value]
     */
    fun <T>coordinateFor(value: T): Float
}