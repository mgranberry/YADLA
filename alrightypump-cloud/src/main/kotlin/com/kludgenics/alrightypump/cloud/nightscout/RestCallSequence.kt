package com.kludgenics.alrightypump.cloud.nightscout

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant

/**
 * Created by matthias on 12/24/15.
 */
class RestCallSequence<T:NightscoutApiEntry>(val range: ClosedRange<Instant>,
                                             val call: (Instant, Instant) -> List<T>) : Sequence<T> {

    override fun iterator(): Iterator<T> {
        return object : Iterator<T> {

            var current = range.endInclusive
            var currentRange  = range.endInclusive.toRecordRange()
            var currentRecords = call(currentRange.start, currentRange.endInclusive).filter { range.contains(it.time.toDateTime(DateTimeZone.UTC).toInstant()) }
            var eventIterator: Iterator<T> = currentRecords.iterator()

            init {
                updateIterator()
            }

            private fun updateIterator(): Boolean {
                if (range.contains(currentRange.start)) {
                    currentRange = currentRange.start.minus(1).toRecordRange()
                    currentRecords = call(currentRange.start, currentRange.endInclusive)
                    if (currentRecords.isEmpty())
                        return false
                    current = currentRecords.last().time.toDateTime(DateTimeZone.UTC).toInstant()
                    eventIterator = currentRecords.iterator()
                    return true
                } else
                    return false
            }

            override fun next(): T {
                if (eventIterator.hasNext()) {
                    return eventIterator.next()
                } else {
                    updateIterator()
                    return eventIterator.next()
                }
            }

            override fun hasNext(): Boolean {
                if (eventIterator.hasNext() == true)
                    return true
                else {
                    return updateIterator()
                }
            }
        }
    }

    data class InstantRange(override val start: Instant,
                            override val endInclusive: Instant) : ClosedRange<Instant>


    fun Instant.toRecordRange(): ClosedRange<Instant> {
        val now = DateTime.now()
        val dateTime = this.toDateTime()
        val dateStart = dateTime.withTimeAtStartOfDay()

        val range = if (dateStart.dayOfYear().roundFloorCopy() == now.dayOfYear().roundFloorCopy()) {
            InstantRange(dateTime.hourOfDay().roundFloorCopy().toInstant(),
                    dateTime.hourOfDay().roundCeilingCopy().toInstant() - 1)
        } else if (now.weekOfWeekyear().roundFloorCopy() == dateStart.weekOfWeekyear().roundFloorCopy()) {
            InstantRange(dateStart.toInstant(), dateStart.plusDays(1).toInstant() - 1)
        } else if (now.monthOfYear().roundFloorCopy() == dateStart.monthOfYear().roundFloorCopy()) {
            InstantRange(dateStart.weekOfWeekyear().roundFloorCopy().toInstant(),
                    dateStart.weekOfWeekyear().roundCeilingCopy().toInstant() - 1)
        } else {
            InstantRange(dateStart.monthOfYear().roundFloorCopy().toInstant(),
                    dateStart.monthOfYear().roundCeilingCopy().toInstant() - 1)
        }
        return range
    }

}