package com.kludgenics.cgmlogger.app.view;

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import com.kludgenics.cgmlogger.app.R
import org.jetbrains.anko.AnkoLogger
import kotlin.properties.Delegates

/**
 * Created by matthiasgranberry on 7/18/15.
 */
public class DailyBgChartView(context: Context, attrs: AttributeSet?, defStyle: Int) : ChartXAxis,
        AnkoLogger, AbstractBgChartView(context, attrs, defStyle) {

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
        throw UnsupportedOperationException()
    }

    override fun getXLabel(xValue: Int): String {
        throw UnsupportedOperationException()
    }

    override val boundedPaths: MutableList<ScaledPaintedPath>
        get() = throw UnsupportedOperationException()
    override var xProgression: IntProgression
        get() = throw UnsupportedOperationException()
        set(value) {
        }

    override protected val highPaint: Paint by Delegates.lazy { initializePaint(R.color.high_line, stroke = true, strokeWidth = dip(2),
            pathEffect = DashPathEffect(floatArrayOf(dip(10), dip(20)), 0f)) }
    override protected val lowPaint: Paint by Delegates.lazy { initializePaint(R.color.low_line, stroke = true, strokeWidth = dip(2),
            pathEffect = DashPathEffect(floatArrayOf(dip(10), dip(20)), 0f)) }
    override protected val targetPaint: Paint by Delegates.lazy { initializePaint(R.color.target_line, stroke = true, strokeWidth = dip(2),
            pathEffect = DashPathEffect(floatArrayOf(dip(10), dip(20)), 0f)) }


}
