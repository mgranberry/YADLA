package com.kludgenics.justgivemeachart

/**
 * Created by matthias on 4/22/16.
 */
interface Axis : Chartjunk {
    object Direction {
        val HORIZONTAL = 1
        val VERTICAL = 2
    }
    var showDescriptions: Boolean
}