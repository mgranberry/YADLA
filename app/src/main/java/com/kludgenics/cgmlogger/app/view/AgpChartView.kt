package com.kludgenics.cgmlogger.app.view

import android.content.Context
import android.graphics.*
import android.support.design.widget.AppBarLayout
import android.util.AttributeSet
import android.view.View
import android.view.ViewManager
import com.kludgenics.cgmlogger.app.R
import com.kludgenics.cgmlogger.app.util.PathParser
import com.kludgenics.cgmlogger.model.math.agp.DailyAgp
import org.jetbrains.anko.*
import kotlin.properties.Delegates

public class AgpChartView(context: Context, attrs: AttributeSet?, defStyle: Int) : AnkoLogger, View(context, attrs, defStyle) {

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
    }

    public constructor(context: Context): this(context, null, 0) {
    }

    var outerPathString: String by Delegates.observable("", {
        propertyMetadata: PropertyMetadata, previous: String, current: String ->
        animatePath(propertyMetadata.name, previous, current)
        outerPath = PathParser.createPathFromPathData(current)
        outerPath.transform(scaleMatrix)
        maybeRequestLayout()
    })

    var innerPathString: String by Delegates.observable("", {
        propertyMetadata: PropertyMetadata, previous: String, current: String ->
        animatePath(propertyMetadata.name, previous, current)
        innerPath = PathParser.createPathFromPathData(current)
        innerPath.transform(scaleMatrix)
    })

    var medianPathString: String by Delegates.observable("", {
        propertyMetadata: PropertyMetadata, previous: String, current: String ->
        animatePath(propertyMetadata.name, previous, current)
        medianPath = PathParser.createPathFromPathData(current)
        medianPath.transform(scaleMatrix)
    })

    var outerPath: Path = Path()
    var innerPath: Path = Path()
    var medianPath: Path = Path()

    var lowPath: Path? = null
    var highPath: Path? = null
    var targetPath: Path? = null

    var lowLine: Int by Delegates.observable(80, {
        propertyMetadata: PropertyMetadata, previous: Int, current: Int ->
        if (current == 0)
            lowPath = null
        else {
            if (lowPath == null)
                lowPath = Path()
            bgLine(lowPath!!, current.toFloat())
        }
    })

    var highLine: Int by Delegates.observable(180, {
        propertyMetadata: PropertyMetadata, previous: Int, current: Int ->
        if (current == 0)
            highPath = null
        else {
            if (highPath == null)
                highPath = Path()
            bgLine(highPath!!, current.toFloat())
        }
    })

    var targetLine: Int by Delegates.observable(110, {
        propertyMetadata: PropertyMetadata, previous: Int, current: Int ->
        if (current == 0)
            targetPath = null
        else {
            if (targetPath == null)
                targetPath = Path()
            bgLine(targetPath!!, current.toFloat())
        }
    })

    private fun bgLine(path: Path, gl: Float) {
        path.rewind()
        val y: Float = (DailyAgp.SPEC_HEIGHT - gl)
        path.moveTo(0f, y)
        path.lineTo(DailyAgp.SPEC_WIDTH, y)
        path.transform(scaleMatrix)
        maybeRequestLayout()
    }

    private fun animatePath(name: String, previous: String, current: String) {
        //throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val outerPaint: Paint by Delegates.lazy { initializePaint(R.color.percentile_outer) }
    private val innerPaint: Paint by Delegates.lazy { initializePaint(R.color.percentile_inner) }
    private val medianPaint: Paint by Delegates.lazy { initializePaint(R.color.percentile_median,
            stroke = true) }
    private val highPaint: Paint by Delegates.lazy { initializePaint(R.color.high_line, stroke = true, strokeWidth = dp2px(2),
            pathEffect = DashPathEffect(floatArrayOf(dp2px(10), dp2px(20)), 0f)) }
    private val lowPaint: Paint by Delegates.lazy { initializePaint(R.color.low_line, stroke = true, strokeWidth = dp2px(2),
            pathEffect = DashPathEffect(floatArrayOf(dp2px(10), dp2px(20)), 0f)) }
    private val targetPaint: Paint by Delegates.lazy { initializePaint(R.color.target_line, stroke = true, strokeWidth = dp2px(2),
            pathEffect = DashPathEffect(floatArrayOf(dp2px(10), dp2px(20)), 0f)) }
    private val cornerEffect by Delegates.lazy { CornerPathEffect(dp2px(20)) }
    private val scaleMatrix: Matrix = Matrix()

    private fun maybeRequestLayout() {
        requestLayout()
    }

    private fun calculateBounds(width: Float, height: Float, scaled: Boolean = false): RectF {
        val bounds = RectF()
        val measurePath = PathParser.createPathFromPathData(outerPathString)
        if (scaled) {
            val sx = width / DailyAgp.SPEC_WIDTH
            val sy = height / DailyAgp.SPEC_HEIGHT

            val matrix = Matrix()
            matrix.setScale(sx, sy)
            measurePath.transform(matrix)
        }
        measurePath.computeBounds(bounds, false)
        return bounds
    }

   override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthType = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthMeasureVal = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightType = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightMeasureVal = View.MeasureSpec.getSize(heightMeasureSpec)

        val paddingHorizontal = paddingLeft + paddingRight
        val paddingVertical = paddingTop + paddingBottom
        val bounds = calculateBounds(widthMeasureVal.toFloat(), heightMeasureVal.toFloat(), false)
        val width = when(widthType) {
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.AT_MOST,
            View.MeasureSpec.EXACTLY -> widthMeasureVal
            else -> widthMeasureVal
        }
        val height = when(heightType) {
            View.MeasureSpec.UNSPECIFIED ->
                    paddingVertical + getContext().dip(Math.max(highLine - lowLine, (bounds.bottom - bounds.top).toInt())).toInt()
            View.MeasureSpec.EXACTLY ->
                    heightMeasureVal
            View.MeasureSpec.AT_MOST ->
                    Math.min(heightMeasureVal, paddingVertical + getContext().dip(bounds.bottom - bounds.top))
            else -> heightMeasureVal
        }
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super<View>.onSizeChanged(w, h, oldw, oldh)
        val width = w.toFloat() - paddingLeft - paddingRight
        val height = h.toFloat() - paddingTop - paddingBottom
        val bounds = calculateBounds(width, height)
        val bbox = RectF(bounds.left, Math.min(DailyAgp.SPEC_HEIGHT - highLine, if (bounds.top != 0f) bounds.top else Float.MAX_VALUE),
                Math.max(bounds.right, DailyAgp.SPEC_WIDTH), Math.max(DailyAgp.SPEC_HEIGHT - lowLine, bounds.bottom))
        info("bounds: $bounds, hbox:$bbox")
        scaleMatrix.setRectToRect(bbox, RectF(paddingLeft.toFloat(), paddingTop.toFloat(),
                w.toFloat() - paddingRight, h.toFloat() - paddingBottom), Matrix.ScaleToFit.FILL)

        // this is ugly. Reapply scale/transform matrices
        outerPathString = outerPathString
        innerPathString = innerPathString
        medianPathString = medianPathString
        lowLine = lowLine
        highLine = highLine
        targetLine = targetLine
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

fun ViewManager.agpChartView(init: AgpChartView.() -> Unit = {}) =
        __dslAddView({ AgpChartView(it) }, init, this)
