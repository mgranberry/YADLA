package com.kludgenics.cgmlogger.app.view

import android.content.Context
import android.graphics.*
import android.support.design.widget.AppBarLayout
import android.util.AttributeSet
import android.view.View
import android.view.ViewManager
import com.kludgenics.cgmlogger.app.R
import com.kludgenics.cgmlogger.app.util.PathParser
import com.kludgenics.cgmlogger.model.math.agp.CachedDatePeriodAgp
import com.kludgenics.cgmlogger.model.math.agp.DailyAgp
import org.jetbrains.anko.*
import org.joda.time.Period
import kotlin.properties.Delegates

public class AgpChartView(context: Context, attrs: AttributeSet?, defStyle: Int) : ChartXAxis,
        AnkoLogger, BgChartView(context, attrs, defStyle) {

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
    }

    public constructor(context: Context): this(context, null, 0) {
    }

    override var xProgression = (0 .. DailyAgp.SPEC_WIDTH.toInt() step 10)

    override var xLabelPeriod = 2
    override var showXAxis = true
    override var xAxisOffset: Float by Delegates.notNull()
    override val xAxisTextSize: Float by Delegates.lazy {
        dip(10)
    }

    override val xAxisPaint: Paint by Delegates.lazy {
        initializePaint(color=Color.BLACK, stroke = true, strokeWidth = dip(2), init={setAlpha(127)})
    }

    override val xAxisLabelPaint: Paint by Delegates.lazy {
        initializePaint(color=Color.BLACK, init={
            setAlpha(127)
            setTextSize(xAxisTextSize)
            setTextAlign(Paint.Align.LEFT)
        })
    }

    override val xAxisTickHeight: Float by Delegates.lazy {
        dip(5)
    }

    override fun getXValue(xValue: Int): Float {
        return paddingLeft + xValue.toFloat()/DailyAgp.SPEC_WIDTH * (getWidth() - paddingLeft - paddingRight)
    }

    override fun getXLabel(xValue: Int): String {
        val hours = xValue.toInt() / 10
        return "${hours}"
    }

    fun updatePath(previous: Array<PathParser.PathDataNode>?, current: Array<PathParser.PathDataNode>,
                   untransformed: Path, dst: Path) {
        if (previous != current) {
            untransformed.rewind()
            PathParser.PathDataNode.nodesToPath(current, untransformed)
        }
        untransformed.transform(scaleMatrix, dst)
    }

    var outerPathData: Array<PathParser.PathDataNode> by Delegates.observable(emptyArray(), {
        propertyMetadata, previous, current ->
        updatePath(previous, current, outerPathUntransformed, outerPath)
    })

    var outerPathString: String
        get() = throw UnsupportedOperationException("not defined")
        set(value) {
            outerPathData = PathParser.createNodesFromPathData(value)
        }

    var innerPathData: Array<PathParser.PathDataNode> by Delegates.observable(emptyArray(), {
        propertyMetadata, previous, current ->
        updatePath(previous, current, innerPathUntransformed, innerPath)
    })

    var innerPathString: String
        get() = throw UnsupportedOperationException("not defined")
        set(value) {
        innerPathData = PathParser.createNodesFromPathData(value)
    }

    var medianPathData: Array<PathParser.PathDataNode> by Delegates.observable(emptyArray(), {
        propertyMetadata, previous, current ->
        updatePath(previous, current, medianPathUntransformed, medianPath)
    })

    var medianPathString: String
        get() = throw UnsupportedOperationException("not defined")
        set(value) {
            medianPathData = PathParser.createNodesFromPathData(value)
        }

    val outerPathUntransformed = Path()
    val outerPath = Path()
    val innerPathUntransformed = Path()
    val innerPath = Path()
    val medianPathUntransformed = Path()
    val medianPath = Path()

    private fun animatePath(name: String, previous: String, current: String) {
        //throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val outerPaint: Paint by Delegates.lazy { initializePaint(R.color.percentile_outer, pathEffect = cornerEffect) }
    private val innerPaint: Paint by Delegates.lazy { initializePaint(R.color.percentile_inner, pathEffect = cornerEffect) }
    private val medianPaint: Paint by Delegates.lazy { initializePaint(R.color.percentile_median,
            stroke = true, pathEffect = cornerEffect) }
    private val highPaint: Paint by Delegates.lazy { initializePaint(R.color.high_line, stroke = true, strokeWidth = dip(2),
            pathEffect = DashPathEffect(floatArrayOf(dip(10), dip(20)), 0f)) }
    private val lowPaint: Paint by Delegates.lazy { initializePaint(R.color.low_line, stroke = true, strokeWidth = dip(2),
            pathEffect = DashPathEffect(floatArrayOf(dip(10), dip(20)), 0f)) }
    private val targetPaint: Paint by Delegates.lazy { initializePaint(R.color.target_line, stroke = true, strokeWidth = dip(2),
            pathEffect = DashPathEffect(floatArrayOf(dip(10), dip(20)), 0f)) }
    private val cornerEffect by Delegates.lazy { CornerPathEffect(dip(20)) }

    private fun calculateBounds(width: Float, height: Float, scaled: Boolean = false): RectF {
        val bounds = RectF()
        val measurePath = outerPathUntransformed
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
                    paddingVertical + getContext().dip(Math.max(highLine - lowLine +
                            (if (showXAxis) xAxisTextSize + xAxisTickHeight else 0f).toInt(),
                            (bounds.bottom - bounds.top  +
                                    (if (showXAxis) xAxisTextSize + xAxisTickHeight else 0f).toInt()).toInt())).toInt()
            View.MeasureSpec.EXACTLY ->
                    heightMeasureVal
            View.MeasureSpec.AT_MOST ->
                    Math.min(heightMeasureVal, (if (showXAxis) xAxisTextSize + xAxisTickHeight else 0f).toInt() + paddingVertical + getContext().dip(bounds.bottom - bounds.top))
            else -> heightMeasureVal
        }
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super<BgChartView>.onSizeChanged(w, h, oldw, oldh)
        val width = w.toFloat() - paddingLeft - paddingRight
        val height = h.toFloat() - paddingTop - paddingBottom
        val bounds = calculateBounds(width, height)
        val bbox = RectF(bounds.left, Math.min(DailyAgp.SPEC_HEIGHT - highLine, if (bounds.top != 0f) bounds.top else Float.MAX_VALUE),
                Math.max(bounds.right, DailyAgp.SPEC_WIDTH), Math.max(DailyAgp.SPEC_HEIGHT - lowLine, bounds.bottom))
        info("bounds: $bounds, hbox:$bbox")
        scaleMatrix.setRectToRect(bbox, RectF(paddingLeft.toFloat(), paddingTop.toFloat(),
                w.toFloat() - paddingRight, h.toFloat() - paddingBottom - if (showXAxis) xAxisTextSize + xAxisTickHeight else 0f), Matrix.ScaleToFit.FILL)
        xAxisOffset = h.toFloat() - paddingBottom - xAxisTextSize - xAxisTickHeight
        // this is ugly. Reapply scale/transform matrices
        outerPathData = outerPathData
        innerPathData = innerPathData
        medianPathData = medianPathData
        lowLine = lowLine
        highLine = highLine
        targetLine = targetLine
    }

    override fun onDraw(canvas: Canvas) {
        super<BgChartView>.onDraw(canvas)
        canvas.drawPath(outerPath, outerPaint)
        canvas.drawPath(innerPath, innerPaint)
        canvas.drawPath(medianPath, medianPaint)
        if (lowPath != null)
            canvas.drawPath(lowPath, lowPaint)
        if (highPath != null)
            canvas.drawPath(highPath, highPaint)
        if (targetPath != null)
            canvas.drawPath(targetPath, targetPaint)
        drawXAxis(canvas)
    }
}

fun ViewManager.agpChartView(init: AgpChartView.() -> Unit = {}) =
        __dslAddView({ AgpChartView(it) }, init, this)
