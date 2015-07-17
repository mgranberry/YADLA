package com.kludgenics.cgmlogger.app.view

import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PathEffect
import android.util.AttributeSet
import android.view.View
import org.jetbrains.anko.*
/**
 * Created by matthiasgranberry on 7/17/15.
 */

open public class BaseChartView(context: Context, attrs: AttributeSet?, defStyle: Int): View(context, attrs, defStyle)  {


    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
    }

    public constructor(context: Context): this(context, null, 0) {
    }

    protected fun dip(pixels: Int): Float = (pixels * (getResources()?.getDisplayMetrics()?.density ?: 0f))

    protected fun initializePaint(colorResource: Int = 0, color: Int = Color.BLACK, stroke: Boolean = false,
                                strokeWidth: Float = dip(3),
                                pathEffect: PathEffect? = null, init: Paint.()->Unit = {}): Paint {
        val paint = Paint()
        paint.setColor(if (colorResource != 0)
            resources!!getColor(colorResource)
        else
            color)
        paint.setAntiAlias(true)
        if (stroke) {
            paint.setStyle(Paint.Style.STROKE)
            paint.setStrokeWidth(strokeWidth)
        } else
            paint.setStyle(Paint.Style.FILL)
        if (pathEffect != null)
            paint.setPathEffect(pathEffect)
        paint.init()
        return paint
    }

    protected val scaleMatrix: Matrix = Matrix()
}