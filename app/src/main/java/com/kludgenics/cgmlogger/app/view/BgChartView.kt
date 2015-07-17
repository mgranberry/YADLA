package com.kludgenics.cgmlogger.app.view

import android.content.Context
import android.graphics.Path
import android.util.AttributeSet
import com.kludgenics.cgmlogger.model.math.agp.DailyAgp
import kotlin.properties.Delegates

/**
 * Created by matthiasgranberry on 7/17/15.
 */

public open class BgChartView(context: Context, attrs: AttributeSet?, defStyle: Int):
        BaseChartView(context, attrs, defStyle) {

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
    }

    public constructor(context: Context): this(context, null, 0) {
    }

    protected var lowPath: Path? = null
    protected var highPath: Path? = null
    protected var targetPath: Path? = null

    protected var lowLine: Int by Delegates.observable(80, {
        propertyMetadata: PropertyMetadata, previous: Int, current: Int ->
        if (current == 0)
            lowPath = null
        else {
            if (lowPath == null)
                lowPath = Path()
            bgLine(lowPath!!, current.toFloat())
        }
    })

    protected var highLine: Int by Delegates.observable(180, {
        propertyMetadata: PropertyMetadata, previous: Int, current: Int ->
        if (current == 0)
            highPath = null
        else {
            if (highPath == null)
                highPath = Path()
            bgLine(highPath!!, current.toFloat())
        }
    })

    protected var targetLine: Int by Delegates.observable(110, {
        propertyMetadata: PropertyMetadata, previous: Int, current: Int ->
        if (current == 0)
            targetPath = null
        else {
            if (targetPath == null)
                targetPath = Path()
            bgLine(targetPath!!, current.toFloat())
        }
    })

    protected fun bgLine(path: Path, gl: Float) {
        path.rewind()
        val y: Float = (DailyAgp.SPEC_HEIGHT - gl)
        path.moveTo(0f, y)
        path.lineTo(DailyAgp.SPEC_WIDTH, y)
        path.transform(scaleMatrix)
    }
}