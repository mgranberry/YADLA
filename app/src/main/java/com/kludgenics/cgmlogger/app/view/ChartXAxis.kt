package com.kludgenics.cgmlogger.app.view

import android.graphics.*

/**
 * Created by matthiasgranberry on 7/16/15.
 */

interface ChartXAxis<X: Comparable<X>> {

    var xProgression: Progression<X>
    var xLabelPeriod: Int
    var showXAxis: Boolean
    val xAxisPath: Path
    val xAxisOffset: Float
    var xAxisPaint: Paint
    val xAxisTickHeight: Float
    val xAxisTickWidth: Float

    val scaleMatrix: Matrix

    fun getXValue(xValue: X): Float
    fun getXLabel(xValue: X): String

    fun getXAxisBounds(bounds: RectF) {
        val label = getXLabel(xProgression.start)
        val textBounds = Rect()
        xAxisPath.computeBounds(bounds, false)
        xAxisPaint.getTextBounds(label, 0, label.length(), textBounds)
        bounds.bottom = textBounds.bottom.toFloat() + bounds.bottom
    }

    fun updateXaxisPath() {
        xAxisPath.rewind()
        with(xAxisPath) {
            moveTo(getXValue(xProgression.start), xAxisOffset)
            lineTo(getXValue(xProgression.end), xAxisOffset)
            xProgression.forEachIndexed { idx, x ->
                val isMajor = (idx % xLabelPeriod) == 0
                val tickHeight = xAxisTickHeight * if (isMajor) 1f else .5f
                val tickWidth = xAxisTickWidth * if (isMajor) 1f else .5f
                val tickX = getXValue(x)
                addRect(tickX - tickWidth, -tickHeight, tickX + tickWidth, tickHeight, Path.Direction.CW)
            }
        }
        xAxisPath.transform(scaleMatrix)
    }

    fun drawXAxis(canvas: Canvas) {
        if (showXAxis) {
            canvas.drawPath(xAxisPath, xAxisPaint)
            xProgression.forEachIndexed { idx, x ->
                if (idx % xLabelPeriod == 0 && x != xProgression.end)
                    canvas.drawText(getXLabel(x), getXValue(x), xAxisOffset+xAxisTickHeight, xAxisPaint)}
        }
    }
}