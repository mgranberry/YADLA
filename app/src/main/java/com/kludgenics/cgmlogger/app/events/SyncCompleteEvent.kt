package com.kludgenics.cgmlogger.app.events

import java.util.*

/**
 * Created by matthias on 4/4/16.
 */

data class SyncCompleteEvent(val serialNumber: String, val nextSync: Date? = null)