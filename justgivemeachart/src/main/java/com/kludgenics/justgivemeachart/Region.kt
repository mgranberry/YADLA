package com.kludgenics.justgivemeachart

/**
 * Created by matthias on 4/22/16.
 */
interface Region: Line {
    fun upperSpreadAt(x: Float): Float
    fun lowerSpreadAt(x: Float): Float
}