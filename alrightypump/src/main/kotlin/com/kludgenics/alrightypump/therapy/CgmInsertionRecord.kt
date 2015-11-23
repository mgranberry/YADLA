package com.kludgenics.alrightypump.therapy

/**
 * Created by matthias on 11/23/15.
 */

interface CgmInsertionRecord : Record {
    val removed: Boolean
    val inserted: Boolean get() = !removed
}