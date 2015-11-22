package com.kludgenics.alrightypump.therapy

import org.joda.time.Instant

interface Record {
    val time: Instant
    val source: String
}