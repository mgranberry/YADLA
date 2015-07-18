package com.kludgenics.cgmlogger.app.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.kludgenics.cgmlogger.model.math.agp.DailyAgp
import org.jetbrains.anko.*
/**
 * Created by matthiasgranberry on 7/17/15.
 */

public abstract class AbstractChartView(context: Context, attrs: AttributeSet?, defStyle: Int): View(context, attrs, defStyle), AnkoLogger  {


    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
    }

    public constructor(context: Context): this(context, null, 0) {
    }

    abstract val boundedPaths: MutableList<ScaledPaintedPath>

    // used only to measure paths in boundedPaths
    private val boundRect: RectF = RectF()
    protected fun computePathBounds(bounds: RectF = RectF(Float.MAX_VALUE, Float.MAX_VALUE,
            Float.MIN_VALUE, Float.MIN_VALUE)): RectF {
        boundedPaths.forEach {
            measurePath ->
            if (!measurePath.unscaled.isEmpty()) {
                measurePath.unscaled.computeBounds(boundRect, false)
                bounds.left = Math.min(bounds.left, boundRect.left)
                bounds.top = Math.min(bounds.top, boundRect.top)
                bounds.right = Math.max(bounds.right, boundRect.right)
                bounds.bottom = Math.max(bounds.bottom, boundRect.bottom)
            }
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
                paddingVertical + dip((bounds.bottom - bounds.top + getVerticalDecoractionSize()).toInt()).toInt()
            View.MeasureSpec.EXACTLY ->
                heightMeasureVal
            View.MeasureSpec.AT_MOST ->
                Math.min(heightMeasureVal, (getVerticalDecoractionSize()).toInt() + paddingVertical + getContext().dip(bounds.bottom - bounds.top))
            else -> heightMeasureVal
        }
        info ("setMeasuredDimensions; ${width} ${height}")
        setMeasuredDimension(width, height)
    }

    protected fun getVerticalDecoractionSize(): Float = 0f

    protected fun dip(pixels: Int): Float = (pixels * (getResources()?.getDisplayMetrics()?.density ?: 0f))

    protected fun initializePaint(colorResource: Int = 0, color: Int = Color.BLACK, stroke: Boolean = false,
                                strokeWidth: Float = dip(3),
                                pathEffect: PathEffect? = null, init: Paint.()->Unit = {}): Paint {
        val paint = Paint()
        paint.setColor(if (colorResource != 0)
            resources!!getColor(colorResource)
        else
            color)
        paint.setAntiAlias(true)
        if (stroke) {
            paint.setStyle(Paint.Style.STROKE)
            paint.setStrokeWidth(strokeWidth)
        } else
            paint.setStyle(Paint.Style.FILL)
        if (pathEffect != null)
            paint.setPathEffect(pathEffect)
        paint.init()
        return paint
    }

    protected val scaleMatrix: Matrix = Matrix()
}