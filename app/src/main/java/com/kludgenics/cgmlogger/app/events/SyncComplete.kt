package com.kludgenics.cgmlogger.app.events

import java.util.*

/**
 * Created by matthias on 4/4/16.
 */

data class SyncComplete(val serialNumber: String, val nextSync: Date? = null)