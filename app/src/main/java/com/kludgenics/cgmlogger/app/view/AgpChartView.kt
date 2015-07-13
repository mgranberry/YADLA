package com.kludgenics.cgmlogger.app.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.kludgenics.cgmlogger.app.R
import com.kludgenics.cgmlogger.app.util.PathParser
import com.kludgenics.cgmlogger.model.math.agp.DailyAgp
import org.jetbrains.anko.*
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
        if (current == 0)
            lowPath = null
        else {
            if (lowPath == null)
                lowPath = Path()
            bgLine(lowPath!!, current.toFloat())
        }
        invalidate()
    })

    var highLine: Int by Delegates.observable(0, {
        propertyMetadata: PropertyMetadata, previous: Int, current: Int ->
        if (current == 0)
            highPath = null
        else {
            if (highPath == null)
                highPath = Path()
            bgLine(highPath!!, current.toFloat())
        }
        invalidate()
    })

    var targetLine: Int by Delegates.observable(0, {
        propertyMetadata: PropertyMetadata, previous: Int, current: Int ->
        if (current == 0)
            targetPath = null
        else {
            if (targetPath == null)
                targetPath = Path()
            bgLine(targetPath!!, current.toFloat())
        }
        invalidate()
    })

    private fun bgLine(path: Path, gl: Float) {
        path.rewind()
        val y: Float = (DailyAgp.SPEC_HEIGHT - gl)
        path.moveTo(0f, y)
        path.lineTo(DailyAgp.SPEC_WIDTH, y)
        path.transform(scaleMatrix)
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
    private val cornerEffect by Delegates.lazy { CornerPathEffect(dp2px(1)) }
    private val scaleMatrix: Matrix = Matrix()

    override fun getSuggestedMinimumHeight(): Int {
        return super<View>.getSuggestedMinimumHeight()
    }

    override fun getSuggestedMinimumWidth(): Int {
        return super<View>.getSuggestedMinimumWidth()
    }

    private fun calculateBounds(width: Float, height: Float): RectF {
        val bounds = RectF()
        val measurePath = PathParser.createPathFromPathData(outerPathString)

        val sx = width / DailyAgp.SPEC_WIDTH
        val sy = height / DailyAgp.SPEC_HEIGHT

        val matrix = Matrix()
        matrix.setScale(sx, sy)
        measurePath.transform(matrix)
        measurePath.computeBounds(bounds, false)
        return bounds
    }

   /*override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthType = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthMeasureVal = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightType = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightMeasureVal = View.MeasureSpec.getSize(heightMeasureSpec)

        val paddingHorizontal = paddingLeft + paddingRight
        val paddingVertical = paddingTop + paddingBottom
        val bounds = calculateBounds(widthMeasureVal.toFloat(), heightMeasureVal.toFloat())

        info("bounds: l:${bounds.left}, t:${bounds.top}, r:${bounds.right}, b:${bounds.bottom}")
        val width = when(widthType) {
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.AT_MOST,
            View.MeasureSpec.EXACTLY -> widthMeasureVal
            else -> widthMeasureVal
        }
        val height = when(heightType) {
            View.MeasureSpec.UNSPECIFIED ->
                    paddingVertical + Math.abs(bounds.bottom - bounds.top).toInt()
            View.MeasureSpec.EXACTLY ->
                    heightMeasureVal
            View.MeasureSpec.AT_MOST ->
                    Math.min(heightMeasureVal, paddingVertical + Math.abs(bounds.bottom - bounds.top).toInt())
            else -> heightMeasureVal
        }
        setMeasuredDimension(width, height)
        info ("onMeasure: $widthMeasureVal/$widthType, $heightMeasureVal/$heightType ($width, $height)")

        //super<View>.onMeasure(View.MeasureSpec.makeMeasureSpec(width, widthType),
        //        View.MeasureSpec.makeMeasureSpec(height, heightType)
    }*/

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super<View>.onSizeChanged(w, h, oldw, oldh)
        val width = w.toFloat() - paddingLeft - paddingRight
        val height = h.toFloat() - paddingTop - paddingBottom
        val bounds = calculateBounds(width, height)
        info ("padding: ($paddingLeft $paddingTop $paddingRight $paddingBottom")
        info ("calcs l: ${bounds.left}, t: ${bounds.top}, r: ${bounds.right}, b: ${bounds.bottom}} ")
        info ("calcs w: $width, h: $height, spec_w: ${DailyAgp.SPEC_WIDTH}, spec:h: ${DailyAgp.SPEC_HEIGHT}")
        //val sx = width * (width / (bounds.right - bounds.left)) / DailyAgp.SPEC_WIDTH
        //val sy = height * (height / (bounds.bottom - bounds.top)) / DailyAgp.SPEC_HEIGHT
        val sx = width / DailyAgp.SPEC_WIDTH
        val sy = height / DailyAgp.SPEC_HEIGHT
        info ("val ${sx} = ${width} * (${width} / (${bounds.right} - ${bounds.left})) / ${DailyAgp.SPEC_WIDTH}")
        info ("Matrix scale: $sx, $sy")
        scaleMatrix.setScale(sx, sy)
        //scaleMatrix.postTranslate(-bounds.left, -bounds.top*1.5f)
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
        //canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        //canvas.translate(bounds.left, bounds.bottom - bounds.top)
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
