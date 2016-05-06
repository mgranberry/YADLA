package com.kludgenics.justgivemeachart

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.Log
import com.kludgenics.justgivemeachart.interpolators.StepInterpolator
import java.util.*

/**
 * Created by matthias on 5/3/16.
 */
class BasicLine<T, U>(override var valueAdapter: ValueAdapter<T, U>? = null,
                      override var description: String? = null,
                      override var primaryPaint: Paint? = null,
                      override var visibility: Int = Ink.Visibility.VISIBLE,
                      var drawPoints: Boolean = true,
                      override var viewport: RectF = RectF()) : Line<T, U> {
    val TAG = BasicLine::class.java.simpleName

    val linePath = Path()
    var interpolator = StepInterpolator(valueAdapter?.scaledPoints ?: emptyList())

    init {
        invalidate()
    }

    override fun invalidate() {
        Log.d(TAG, "-----Getting scaled points-----")
        val points = valueAdapter?.scaledPoints
        Log.d(TAG, "-----Configuring Interpolator-----")
        interpolator.values = valueAdapter?.scaledPoints ?: emptyList()
        linePath.rewind()
        primaryPaint?.style = Paint.Style.STROKE
        Log.d(TAG, "-----Adding Points-----")
        Log.d(TAG, "viewport: $viewport")
        var shouldMove = true
        var x: Float = 0f
        var y: Float = 0f
        points?.forEach {
            if (it != null) {
                x = it.index * viewport.right
                y = viewport.bottom - (baselineValueAt(it.index) * viewport.bottom)
                if (shouldMove) {
                    linePath.moveTo(x, y)
                }
                else {
                    linePath.lineTo(x, y)
                }
                if (drawPoints || shouldMove)
                    linePath.addCircle(x, y, primaryPaint?.strokeWidth ?: 1f, Path.Direction.CW)
                shouldMove = false
            } else {
                linePath.addCircle(x, y, primaryPaint?.strokeWidth ?: 1f, Path.Direction.CW)
                shouldMove = true
            }
        }
        Log.d(TAG, "-----Done-----")
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(linePath, primaryPaint)
    }

    override fun baselineValueAt(index: Float): Float {
        return interpolator.interpolate(index).value
    }
}