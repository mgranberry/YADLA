package com.kludgenics.cgmlogger.app.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.ViewManager
import com.kludgenics.cgmlogger.app.util.PathParser
import org.jetbrains.anko.*
import kotlin.properties.Delegates

/**
 * Created by matthiasgranberry on 7/19/15.
 */

public class BgRiChartView(context: Context, attrs: AttributeSet?, defStyle: Int) :
        AnkoLogger, AbstractChartView(context, attrs, defStyle) {

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
    }

    public constructor(context: Context): this(context, null, 0) {
    }

    override val boundedPaths: MutableList<ScaledPaintedPath> by lazy(LazyThreadSafetyMode.NONE) { arrayListOf(hbgPath, lbgPath) }
    val cornerEffect: PathEffect by lazy(LazyThreadSafetyMode.NONE) { CornerPathEffect(dip(10)) }

    var hbgPathData: Array<PathParser.PathDataNode> by Delegates.observable(emptyArray(), {
        propertyMetadata, previous, current ->
        updatePath(previous, current, hbgPath)
    })

    var hbgPathString: String
        get() = throw UnsupportedOperationException("not defined")
        set(value) {
            hbgPathData = PathParser.createNodesFromPathData(value)
        }

    var lbgPathData: Array<PathParser.PathDataNode> by Delegates.observable(emptyArray(), {
        propertyMetadata, previous, current ->
        updatePath(previous, current, lbgPath)
    })

    var lbgPathString: String
        get() = throw UnsupportedOperationException("not defined")
        set(value) {
            lbgPathData = PathParser.createNodesFromPathData(value)
        }

    val hbgPath by lazy(LazyThreadSafetyMode.NONE) {
        ScaledPaintedPath(unscaled = Path(),
                scaleMatrix = scaleMatrix,
                paint = initializePaint(color = Color.YELLOW, pathEffect = cornerEffect))
    }

    val lbgPath by lazy(LazyThreadSafetyMode.NONE) {
        ScaledPaintedPath(unscaled = Path(),
                scaleMatrix = scaleMatrix,
                paint = initializePaint(color = Color.RED, pathEffect = cornerEffect))
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val bounds = computePathBounds()
        val target = RectF(paddingLeft.toFloat(), paddingTop.toFloat(),
                w.toFloat() - paddingRight, h.toFloat() - paddingBottom)
        info("Computed bounds: $bounds, target: $target")
        scaleMatrix.setRectToRect(bounds, target, Matrix.ScaleToFit.FILL)
        boundedPaths.forEach { path -> path.invalidate() }
    }

}

fun ViewManager.bgriChartView(init: BgRiChartView.() -> Unit = {}) =
        __dslAddView({ BgRiChartView(it) }, init, this)
