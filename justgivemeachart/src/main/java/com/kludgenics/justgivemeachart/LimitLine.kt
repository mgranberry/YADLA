package com.kludgenics.justgivemeachart

import android.graphics.Paint

abstract class LimitLine(override var description: String?, val limit: Float, paint: Paint? = null) : Line<Any, Any> {
    override var primaryPaint: Paint? = paint
    override var visibility: Int = Ink.Visibility.VISIBLE
    override fun baselineValueAt(index: Float) = limit
}