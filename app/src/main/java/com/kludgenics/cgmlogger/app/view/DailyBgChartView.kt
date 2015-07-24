package com.kludgenics.cgmlogger.app.view;

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.ViewManager
import com.kludgenics.cgmlogger.app.R
import com.kludgenics.cgmlogger.app.util.PathParser
import com.kludgenics.cgmlogger.model.math.agp.DailyAgp
import org.jetbrains.anko.*
import kotlin.properties.Delegates

/**
 * Created by matthiasgranberry on 7/18/15.
 */
public class DailyBgChartView(context: Context, attrs: AttributeSet?, defStyle: Int) : ChartXAxis,
        AnkoLogger, AbstractBgChartView(context, attrs, defStyle) {


    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
    }

    public constructor(context: Context): this(context, null, 0) {
    }

    private val cornerEffect by Delegates.lazy { CornerPathEffect(dip(10)); null }

    override var xLabelPeriod = 1
    override var showXAxis = true
    override var xAxisOffset: Float by Delegates.notNull()
    override val xAxisTextSize: Float by Delegates.lazy { dip(10) }
    override val xAxisPaint: Paint by Delegates.lazy {
        initializePaint(color= Color.BLACK, stroke = true, strokeWidth = dip(2), init={setAlpha(127)}) }
    override val xAxisLabelPaint: Paint by Delegates.lazy {
        initializePaint(color=Color.BLACK, init={
            setAlpha(127)
            setTextSize(xAxisTextSize)
            setTextAlign(Paint.Align.LEFT)
        })
    }
    override val xAxisTickHeight: Float by Delegates.lazy { dip(5) }

    override fun getXValue(xValue: Int): Float {
        return paddingLeft + xValue.toFloat()/ DailyAgp.SPEC_WIDTH * (getWidth() - paddingLeft - paddingRight)
    }

    override fun getXLabel(xValue: Int): String {
        val hours = xValue.toInt() / 10
        return "${hours}"
    }

    var trendPathData: Array<PathParser.PathDataNode> by Delegates.observable(emptyArray(), {
        propertyMetadata, previous, current ->
        updatePath(previous, current, trendPath)
    })

    var trendPathString: String
        get() = throw UnsupportedOperationException("not defined")
        set(value) {
            trendPathData = PathParser.createNodesFromPathData(value)
        }

    val trendPath by Delegates.lazy {
        ScaledPaintedPath(unscaled = Path(),
                scaleMatrix = scaleMatrix,
                paint = initializePaint(R.color.percentile_inner, stroke = true,
                        pathEffect = cornerEffect))
    }



    override val boundedPaths: MutableList<ScaledPaintedPath> = arrayListOf(trendPath)
    override var xProgression = (0 .. DailyAgp.SPEC_WIDTH.toInt() step 10)

    override protected val highPaint: Paint by Delegates.lazy { initializePaint(R.color.high_line, stroke = true, strokeWidth = dip(2),
            pathEffect = DashPathEffect(floatArrayOf(dip(10), dip(20)), 0f)) }
    override protected val lowPaint: Paint by Delegates.lazy { initializePaint(R.color.low_line, stroke = true, strokeWidth = dip(2),
            pathEffect = DashPathEffect(floatArrayOf(dip(10), dip(20)), 0f)) }
    override protected val targetPaint: Paint by Delegates.lazy { initializePaint(R.color.target_line, stroke = true, strokeWidth = dip(2),
            pathEffect = DashPathEffect(floatArrayOf(dip(10), dip(20)), 0f)) }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super<AbstractBgChartView>.onSizeChanged(w, h, oldw, oldh)
        val bounds = computePathBounds()
        scaleMatrix.setRectToRect(bounds, RectF(paddingLeft.toFloat(), paddingTop.toFloat(),
                w.toFloat() - paddingRight, h.toFloat() - paddingBottom - if (showXAxis) xAxisTextSize + xAxisTickHeight else 0f), Matrix.ScaleToFit.FILL)
        xAxisOffset = h.toFloat() - paddingBottom - xAxisTextSize - xAxisTickHeight
        boundedPaths.forEach { path -> path.invalidate() }
    }

    override fun onDraw(canvas: Canvas) {
        super<AbstractBgChartView>.onDraw(canvas)
        drawXAxis(canvas)
    }

}


fun ViewManager.dailyBgChartView(init: DailyBgChartView.() -> Unit = {}) =
        __dslAddView({ DailyBgChartView(it) }, init, this)
