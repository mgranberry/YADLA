package com.kludgenics.alrightypump.therapy

/**
 * Created by matthias on 12/2/15.
 */
/**
 * Created by matthias on 11/23/15.
 */

interface ConsumableRecord : Record

interface CgmInsertionRecord : ConsumableRecord {
    val removed: Boolean
    val inserted: Boolean get() = !removed
}

interface CartridgeChangeRecord : ConsumableRecord {

}

interface CannulaChangedRecord: ConsumableRecord {

}