package com.kludgenics.cgmlogger.app.view

import android.content.Context
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.dip
import org.jetbrains.anko.info

/**
 * Created by matthias on 3/28/16.
 */
abstract class AbstractChartView(context: Context, attrs: AttributeSet?, defStyle: Int): View(context, attrs, defStyle), AnkoLogger {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
    }

    constructor(context: Context) : this(context, null, 0) {
    }

    class PaintedPath(val path: Path, val paint: Paint) {
        val bounds: RectF by lazy {
            val rect = RectF ()
            path.computeBounds(rect, false)
            rect
        }
    }

    abstract val paths: List<PaintedPath>
    abstract val verticalDecorationSize: Int

    private val boundRect = RectF()
    protected fun computePathBounds(bounds: RectF = RectF()): RectF {
        boundRect.setEmpty()
        paths.filter { !it.path.isEmpty }.forEach {
            it.path.computeBounds(bounds, false)
            boundRect.union(bounds)
        }
        return bounds
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthType = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthMeasureVal = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightType = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightMeasureVal = View.MeasureSpec.getSize(heightMeasureSpec)

        val paddingVertical = paddingTop + paddingBottom
        val bounds = computePathBounds()
        val width = when(widthType) {
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.AT_MOST,
            View.MeasureSpec.EXACTLY ->
                widthMeasureVal
            else -> widthMeasureVal
        }
        val height = when(heightType) {
            View.MeasureSpec.UNSPECIFIED ->
                paddingVertical + dip((bounds.bottom - bounds.top + verticalDecorationSize).toInt()).toInt()
            View.MeasureSpec.EXACTLY ->
                heightMeasureVal
            View.MeasureSpec.AT_MOST ->
                Math.min(heightMeasureVal, verticalDecorationSize.toInt() + paddingVertical + context.dip(bounds.bottom - bounds.top))
            else -> heightMeasureVal
        }
        info ("setMeasuredDimensions; $width $height")
        setMeasuredDimension(width, height)
    }

}