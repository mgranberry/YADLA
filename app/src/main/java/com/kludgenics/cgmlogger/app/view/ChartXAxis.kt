package com.kludgenics.cgmlogger.app.view

import android.graphics.*

/**
 * Created by matthiasgranberry on 7/16/15.
 */

interface ChartXAxis {

    var xProgression: IntProgression
    var xLabelPeriod: Int
    var showXAxis: Boolean

    var xAxisOffset: Float
    val xAxisPaint: Paint
    val xAxisLabelPaint: Paint
    val xAxisTickHeight: Float
    val xAxisTextSize: Float
    fun getXValue(xValue: Int): Float
    fun getXLabel(xValue: Int): String
    fun getWidth(): Int

    fun drawXAxis(canvas: Canvas) {
        if (showXAxis) {
            canvas.drawLine(0f, xAxisOffset, canvas.width.toFloat(), xAxisOffset, xAxisPaint)

            xProgression.forEachIndexed { idx, x ->
                val xValue = getXValue(x)
                if (idx % xLabelPeriod == 0) {
                    canvas.drawLine(xValue, xAxisOffset - xAxisTickHeight, xValue, xAxisOffset + xAxisTickHeight, xAxisPaint)
                    if (x != xProgression.last)
                        canvas.drawText(getXLabel(x), xValue + xAxisPaint.strokeWidth, xAxisOffset + xAxisTickHeight/2 + xAxisTextSize, xAxisLabelPaint)
                } else {
                    canvas.drawLine(xValue, xAxisOffset - xAxisTickHeight/2, xValue, xAxisOffset + xAxisTickHeight/2, xAxisPaint)
                }
            }
        }
    }
}