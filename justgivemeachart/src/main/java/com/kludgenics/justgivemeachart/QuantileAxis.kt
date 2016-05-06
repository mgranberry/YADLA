package com.kludgenics.justgivemeachart

import android.graphics.*
import android.util.Log
import kotlin.properties.Delegates

/**
 * Created by matthias on 4/27/16.
 */

class QuantileAxis<T, U>(viewport: RectF, override var description: String?, override var primaryPaint: Paint?) : Axis, DataInk<T, U> {
    override var valueAdapter: ValueAdapter<T, U>? = null
    override var showDescriptions: Boolean = false

    private var _medianPaint: Paint? = null
    val medianPaint: Paint get() {
        var paint: Paint? = _medianPaint
        if (paint != null)
            return paint
        else {
            paint = Paint(primaryPaint)
            val p: Paint = paint
            p.style = Paint.Style.STROKE
            p.strokeWidth = 12f
            _medianPaint = paint
            return paint
        }
    }

    private var _quartilePaint: Paint? = null
    val quartilePaint: Paint get() {
        var paint: Paint? = _quartilePaint
        if (paint != null)
            return paint
        else {
            paint = Paint(primaryPaint)
            val p: Paint = paint
            p.alpha = 127
            p.style = Paint.Style.STROKE
            p.strokeWidth = 24f
            _quartilePaint = paint
            return paint
        }
    }

    private var _decilePaint: Paint? = null
    val decilePaint: Paint get() {
        var paint: Paint? = _decilePaint
        if (paint != null)
            return paint
        else {
            paint = Paint(primaryPaint)
            val p: Paint = paint
            p.alpha = 63
            p.style = Paint.Style.STROKE
            p.strokeWidth = 12f
            _quartilePaint = paint
            return paint
        }
    }

    override var viewport : RectF by Delegates.observable(viewport) {
        property, newValue, oldValue ->
        updatePath(interDecilePath, firstDecile, ninthDecile)
        updatePath(interQuartilePath, firstQuartile, thirdQuartile)
    }

    var median: Float = Float.NaN

    var firstQuartile: Float by Delegates.observable(Float.NaN) { property, old, new ->
        if (old != new)
            updatePath(interQuartilePath, firstQuartile, thirdQuartile)
    }

    var thirdQuartile: Float by Delegates.observable(Float.NaN) { property, old, new ->
        if (old != new)
            updatePath(interQuartilePath, firstQuartile, thirdQuartile)
    }

    var firstDecile: Float by Delegates.observable(Float.NaN) { property, old, new ->
        if (old != new)
            updatePath(interDecilePath, firstDecile, ninthDecile)
    }

    var ninthDecile: Float by Delegates.observable(Float.NaN) { property, old, new ->
        if (old != new)
            updatePath(interDecilePath, firstDecile, ninthDecile)
    }

    val interQuartilePath: Path by lazy {
        updatePath(Path(), firstQuartile, thirdQuartile)
    }

    val interDecilePath: Path by lazy {
        updatePath(Path(), firstDecile, ninthDecile)
    }

    private fun updatePath(path: Path, start: Float, end: Float): Path {
        path.rewind()
        if (start != Float.NaN && end != Float.NaN) {
            val startScaled = start * (viewport.bottom - viewport.top)
            val endScaled = end * (viewport.bottom - viewport.top)
            path.moveTo(viewport.left, startScaled)
            path.lineTo(viewport.left, endScaled)
            path.close()
        }
        return path
    }

    override fun onDraw(canvas: Canvas) {
        //super<Axis>.onDraw(canvas)
        canvas.drawPath(interDecilePath, decilePaint)
        canvas.drawPath(interQuartilePath, quartilePaint)
        canvas.drawCircle(0f, median, 14f, medianPaint)
    }

    override var visibility: Int = Ink.Visibility.VISIBLE
}