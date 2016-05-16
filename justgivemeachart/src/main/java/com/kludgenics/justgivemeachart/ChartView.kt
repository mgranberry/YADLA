package com.kludgenics.justgivemeachart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.util.*

/**
 * Created by matthias on 4/27/16.
 */
class ChartView : View {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val primaryPaint = Paint()
    init {
        primaryPaint.color = Color.BLACK
    }

    val quantileAxis = QuantileAxis<Any, Any>(RectF(clipBounds), "foo", primaryPaint)
    init {
        quantileAxis.firstDecile = .1f
        quantileAxis.firstQuartile = .2f
        quantileAxis.median = .35f
        quantileAxis.thirdQuartile =.75f
        quantileAxis.ninthDecile = .9f
    }

    private val lines : MutableList<Line<*, *>> = ArrayList()

    fun setLines(vararg newLines: Line<*, *>) {
        lines.clear()
        lines.addAll(newLines)
        updateLines()
    }

    fun updateLines() {
        val viewport = RectF(0f, 0f, width.toFloat(), height.toFloat())
        lines.forEach {
            it.viewport = viewport
            it.invalidate()
            Log.d("ChartView", "Setting viewport to $viewport")
        }
        postInvalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.d("ChartView", "onLayout w: $width h: $height ")
        updateLines()
    }

    override fun onDraw(canvas: Canvas) {
        Log.d("foo", "${canvas.clipBounds.left} ${canvas.clipBounds.right} ${canvas.clipBounds.top} ${canvas.clipBounds.bottom}")
        quantileAxis.medianPaint.color = Color.TRANSPARENT
        Log.d("foo", "viewport: ${quantileAxis.viewport}")
        quantileAxis.viewport = RectF(canvas.clipBounds)
        quantileAxis.onDraw(canvas)
        lines.forEach { it.onDraw(canvas) }
    }
}