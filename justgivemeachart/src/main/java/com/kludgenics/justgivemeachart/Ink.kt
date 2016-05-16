package com.kludgenics.justgivemeachart

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

/**
 * Created by matthias on 4/22/16.
 */
interface Ink {
    object Visibility {
        const val INVISIBLE = 0
        const val VISIBLE = 4
        const val GONE = 8
    }
    var description: String?
    var primaryPaint: Paint?
    var visibility: Int
    var viewport : RectF

    fun onDraw(canvas: Canvas)
}