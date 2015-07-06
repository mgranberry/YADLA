package com.kludgenics.cgmlogger.model.math

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.RealmClass
import org.joda.time.DateTime
import java.util.*

/**
 * Created by matthiasgranberry on 7/5/15.
 */

@RealmClass public open class CachedDatePeriodAgp(public open var outer: String = "",
                                                  public open var inner: String = "",
                                                  public open var median: String = "",
                                                  public open var date: Date = Date(),
                                                  public open var period: Int = 30): RealmObject() {
    @Ignore
    public var svgHeight: Int = 360
    @Ignore
    public var svgWidth: Int = 240
}

public var CachedDatePeriodAgp.dateTime: DateTime
    get() = DateTime(date)
    set(value) { date = value.toDate() }


public val CachedDatePeriodAgp.svg: String
    get() =
            """
<?xml version="1.0" encoding="utf-8" standalone="no"?>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<svg height="${svgHeight}pt" version="1.1" viewBox="0 0 240 360" width="${svgWidth}pt" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
    <g id="agp">
        <path d="${outer}" fill="#2d95c2"/>
        <path d="${inner}" fill="#005882"/>
        <path d="${median}" stroke="#bce6ff" fill-opacity="0.0" stroke-with="3"/>
        <line x1="0" y1="200" y2="200" x2="360" stroke="yellow"/>
        <line x1="0" y1="280" y2="280" x2="360" stroke="red"/>
    </g>
</svg>
"""
