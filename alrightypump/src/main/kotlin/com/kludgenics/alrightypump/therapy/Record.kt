package com.kludgenics.alrightypump.therapy

import org.joda.time.LocalDateTime

interface Record {
    val id: String?
    val time: LocalDateTime
    val source: String
}