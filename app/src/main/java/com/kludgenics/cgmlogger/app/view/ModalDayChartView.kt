package com.kludgenics.cgmlogger.app.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.kludgenics.cgmlogger.app.R
import com.kludgenics.cgmlogger.model.math.agp.DailyAgp
import org.jetbrains.anko.*
import kotlin.properties.Delegates

/**
 * Created by matthiasgranberry on 7/19/15.
 */

public class ModalDayChartView(context: Context, attrs: AttributeSet?, defStyle: Int) : ChartXAxis,
        AnkoLogger, AbstractBgChartView(context, attrs, defStyle) {

    private val cornerEffect by lazy(LazyThreadSafetyMode.NONE) { CornerPathEffect(dip(10)) }

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
    }

    public constructor(context: Context): this(context, null, 0) {
    }

    override var xProgression = (0 .. DailyAgp.SPEC_WIDTH.toInt() step 10)
    override var xLabelPeriod = 2
    override var showXAxis = true
    override var xAxisOffset: Float by Delegates.notNull()
    override val xAxisTextSize: Float by lazy(LazyThreadSafetyMode.NONE) { dip(10) }
    override val xAxisPaint: Paint by lazy(LazyThreadSafetyMode.NONE) {
        initializePaint(color= Color.BLACK, stroke = true, strokeWidth = dip(2), init={ alpha = 127 }) }

    override val xAxisLabelPaint: Paint by lazy(LazyThreadSafetyMode.NONE) {
        initializePaint(color= Color.BLACK, init={
            alpha = 127
            textSize = xAxisTextSize
            textAlign = Paint.Align.LEFT
        })
    }

    override val xAxisTickHeight: Float by lazy(LazyThreadSafetyMode.NONE) { dip(5) }

    override fun getXValue(xValue: Int): Float {
        return paddingLeft + xValue.toFloat()/ DailyAgp.SPEC_WIDTH * (width - paddingLeft - paddingRight)
    }

    override fun getXLabel(xValue: Int): String {
        val hours = xValue.toInt() / 10
        return "$hours"
    }

    override protected val highPaint: Paint by lazy(LazyThreadSafetyMode.NONE) { initializePaint(R.color.high_line, stroke = true, strokeWidth = dip(2),
            pathEffect = DashPathEffect(floatArrayOf(dip(10), dip(20)), 0f)) }
    override protected val lowPaint: Paint by lazy { initializePaint(R.color.low_line, stroke = true, strokeWidth = dip(2),
            pathEffect = DashPathEffect(floatArrayOf(dip(10), dip(20)), 0f)) }
    override protected val targetPaint: Paint by lazy(LazyThreadSafetyMode.NONE) { initializePaint(R.color.target_line, stroke = true, strokeWidth = dip(2),
            pathEffect = DashPathEffect(floatArrayOf(dip(10), dip(20)), 0f)) }

    override val boundedPaths: MutableList<ScaledPaintedPath> = arrayListOf()

    private fun animatePath(name: String, previous: String, current: String) {
        //throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val bounds = computePathBounds()
        scaleMatrix.setRectToRect(bounds, RectF(paddingLeft.toFloat(), paddingTop.toFloat(),
                w.toFloat() - paddingRight, h.toFloat() - paddingBottom - if (showXAxis) xAxisTextSize + xAxisTickHeight else 0f), Matrix.ScaleToFit.FILL)
        xAxisOffset = h.toFloat() - paddingBottom - xAxisTextSize - xAxisTickHeight
        boundedPaths.forEach { path -> path.invalidate() }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawXAxis(canvas)
    }

}