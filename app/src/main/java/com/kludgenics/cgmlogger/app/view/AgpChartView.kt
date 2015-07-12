package com.kludgenics.cgmlogger.app.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.kludgenics.cgmlogger.app.R
import com.kludgenics.cgmlogger.app.util.PathParser
import com.kludgenics.cgmlogger.model.math.agp.DailyAgp
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.resources
import kotlin.properties.Delegates

public class AgpChartView(context: Context, attrs: AttributeSet?, defStyle: Int) : AnkoLogger, View(context, attrs, defStyle) {

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
    }

    var outerPathString: String by Delegates.observable("", {
        propertyMetadata: PropertyMetadata, previous: String, current: String ->
        animatePath(propertyMetadata.name, previous, current)
        outerPath = PathParser.createPathFromPathData(current)
        outerPath.transform(scaleMatrix)
        invalidate()
    })

    var innerPathString: String by Delegates.observable("", {
        propertyMetadata: PropertyMetadata, previous: String, current: String ->
        animatePath(propertyMetadata.name, previous, current)
        innerPath = PathParser.createPathFromPathData(current)
        innerPath.transform(scaleMatrix)
        invalidate()
    })

    var medianPathString: String by Delegates.observable("", {
        propertyMetadata: PropertyMetadata, previous: String, current: String ->
        animatePath(propertyMetadata.name, previous, current)
        medianPath = PathParser.createPathFromPathData(current)
        medianPath.transform(scaleMatrix)
        invalidate()
    })

    var outerPath: Path = Path()
    var innerPath: Path = Path()
    var medianPath: Path = Path()

    var lowPath: Path? = null
    var highPath: Path? = null
    var targetPath: Path? = null

    var lowLine: Int by Delegates.observable(0, {
        propertyMetadata: PropertyMetadata, previous: Int, current: Int ->
        if (lowPath == null)
            lowPath = Path()
        bgLine(lowPath!!, current.toFloat())
        invalidate()
    })

    var highLine: Int by Delegates.observable(0, {
        propertyMetadata: PropertyMetadata, previous: Int, current: Int ->
        if (highPath == null)
            highPath = Path()
        bgLine(highPath!!, current.toFloat())
        invalidate()
    })

    var targetLine: Int by Delegates.observable(0, {
        propertyMetadata: PropertyMetadata, previous: Int, current: Int ->
        if (targetPath == null)
            targetPath = Path()
        bgLine(targetPath!!, current.toFloat())
        invalidate()
    })


    private fun bgLine(path: Path, gl: Float) {
        val sy = this.getHeight().toFloat() / DailyAgp.SPEC_HEIGHT
        path.rewind()
        path.moveTo(0f, getHeight() - (gl * sy))
        path.lineTo(getWidth().toFloat(), getHeight() - (gl * sy))

    }

    private fun animatePath(name: String, previous: String, current: String) {
        //throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val outerPaint: Paint by Delegates.lazy { initializePaint(R.color.percentile_outer) }
    private val innerPaint: Paint by Delegates.lazy { initializePaint(R.color.percentile_inner) }
    private val medianPaint: Paint by Delegates.lazy { initializePaint(R.color.percentile_median,
            stroke = true) }
    private val highPaint: Paint by Delegates.lazy { initializePaint(R.color.high_line, stroke = true,
            pathEffect = DashPathEffect(floatArrayOf(dp2px(10), dp2px(20)), 0f)) }
    private val lowPaint: Paint by Delegates.lazy { initializePaint(R.color.low_line, stroke = true,
            pathEffect = DashPathEffect(floatArrayOf(dp2px(10), dp2px(20)), 0f)) }
    private val targetPaint: Paint by Delegates.lazy { initializePaint(R.color.target_line, stroke = true,
            pathEffect = DashPathEffect(floatArrayOf(dp2px(10), dp2px(20)), 0f)) }
    private val cornerEffect by Delegates.lazy { CornerPathEffect(dp2px(10)) }
    private val scaleMatrix: Matrix = Matrix()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super<View>.onLayout(changed, left, top, right, bottom)
        val sx = this.getWidth().toFloat() / DailyAgp.SPEC_WIDTH
        val sy = this.getHeight().toFloat() / DailyAgp.SPEC_HEIGHT
        info ("Matrix scale: $sx, $sy")
        scaleMatrix.setScale(sx, sy)
        outerPathString = outerPathString
        innerPathString = innerPathString
        medianPathString = medianPathString
        lowLine = 80
        highLine = 180
        targetLine = 110

    }

    private fun dp2px(dp: Int): Float {
        val m = resources?.getDisplayMetrics()
        return if (m != null) dp * m.densityDpi / 160f else Float.NaN
    }

    private fun initializePaint(colorResource: Int, stroke: Boolean = false,
                                strokeWidth: Float = dp2px(3),
                                pathEffect: PathEffect = cornerEffect): Paint {
        val paint = Paint()
        paint.setColor(resources!!getColor(colorResource))
        paint.setAntiAlias(true)
        if (stroke) {
            paint.setStyle(Paint.Style.STROKE)
            paint.setStrokeWidth(strokeWidth)
        } else
            paint.setStyle(Paint.Style.FILL)
        paint.setPathEffect(pathEffect)
        return paint
    }

    override fun onDraw(canvas: Canvas) {
        super<View>.onDraw(canvas)
        canvas.drawPath(outerPath, outerPaint)
        canvas.drawPath(innerPath, innerPaint)
        canvas.drawPath(medianPath, medianPaint)
        if (lowPath != null)
            canvas.drawPath(lowPath, lowPaint)
        if (highPath != null)
            canvas.drawPath(highPath, highPaint)
        if (targetPath != null)
            canvas.drawPath(targetPath, targetPaint)
    }
}
