package com.kludgenics.cgmlogger.model.math.trendline

import io.realm.RealmObject
import io.realm.annotations.RealmClass
import org.joda.time.Period
import java.util.*

/**
 * Created by matthiasgranberry on 7/18/15.
 */

@RealmClass public open class CachedPeriod(public open var highLine: String = "",
                                           public open var lowLine: String = "",
                                           public open var inRangeLine: String = "",
                                           public open var highCount: Int = 0,
                                           public open var lowCount: Int = 0,
                                           public open var inRangeCount: Int = 0,
                                           public open var totalCount: Int = highCount + inRangeCount + lowCount,
                                           public open var low: Int = 80,
                                           public open var high: Int = 180,
                                           public open var date: Date = Date(),
                                           public open var period: Int = 1) : RealmObject() {

}