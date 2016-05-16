package com.kludgenics.justgivemeachart

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kotlin.properties.Delegates

/**
 * Created by matthias on 5/3/16.
 */
class TimeAxis(viewport: RectF, override var description: String?, override var primaryPaint: Paint?) : Axis {
    override var visibility: Int = Ink.Visibility.VISIBLE
    override var showDescriptions: Boolean = false

    override var viewport : RectF by Delegates.observable(viewport) {
        property, newValue, oldValue ->
    }
    override fun onDraw(canvas: Canvas) {
        throw UnsupportedOperationException()
    }
}