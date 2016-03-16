package com.kludgenics.cgmlogger.app.view

import android.content.Context
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import com.kludgenics.cgmlogger.model.math.agp.DailyAgp
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

/**
 * Created by matthiasgranberry on 7/17/15.
 */

public abstract class AbstractBgChartView(context: Context, attrs: AttributeSet?, defStyle: Int):
        AbstractChartView(context, attrs, defStyle), AnkoLogger {

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
    }

    public constructor(context: Context): this(context, null, 0) {
    }

    abstract protected val lowPaint: Paint
    abstract protected val highPaint: Paint
    abstract protected val targetPaint: Paint

    protected var lowPath: ScaledPaintedPath? = null
    protected var highPath: ScaledPaintedPath? = null
    protected var targetPath: ScaledPaintedPath? = null

    public var lowLine: Int by Delegates.observable(80, {
        propertyMetadata: KProperty<*>, previous: Int, current: Int ->
        info("setting lowLine to $current")
        if (current == 0) {
            boundedPaths.remove(lowPath)
            lowPath = null
        }
        else {
            if (lowPath == null) {
                lowPath = ScaledPaintedPath(unscaled=Path(), scaleMatrix = scaleMatrix, paint = lowPaint)
                boundedPaths.add(lowPath!!)
            }
            bgLine(lowPath!!, current.toFloat())
        }
    })

    public var targetLine: Int by Delegates.observable(110, {
        propertyMetadata: KProperty<*>, previous: Int, current: Int ->
        info("setting targetLine to $current")
        if (current == 0) {
            boundedPaths.remove(targetPath)
            targetPath = null
        }
        else {
            if (targetPath == null) {
                targetPath = ScaledPaintedPath(unscaled=Path(), scaleMatrix = scaleMatrix, paint = targetPaint)
                boundedPaths.add(targetPath!!)
            }
            bgLine(targetPath!!, current.toFloat())
        }
    })

    public var highLine: Int by Delegates.observable(180, {
        propertyMetadata: KProperty<*>, previous: Int, current: Int ->
        info("setting highLine to $current")
        if (current == 0) {
            boundedPaths.remove(highPath)
            highPath = null
        }
        else {
            if (highPath == null) {
                highPath = ScaledPaintedPath(unscaled=Path(), scaleMatrix = scaleMatrix, paint = highPaint)
                boundedPaths.add(highPath!!)
            }
            bgLine(highPath!!, current.toFloat())
        }
    })

    protected fun bgLine(path: ScaledPaintedPath, gl: Float) {
        path.unscaled.rewind()
        val y: Float = (DailyAgp.SPEC_HEIGHT - gl)
        path.unscaled.moveTo(0f, y)
        path.unscaled.lineTo(DailyAgp.SPEC_WIDTH, y)
        path.invalidate()
    }
}