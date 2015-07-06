package com.kludgenics.cgmlogger.app.view

import android.content.Context
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.kludgenics.cgmlogger.app.R
import org.jetbrains.anko.resources
import kotlin.properties.Delegates

public class AgpChartView(context: Context, attrs: AttributeSet?, defStyle: Int) : View(context, attrs, defStyle) {
    companion object {
        val MODEL_HEIGHT = 256
        val MODEL_WIDTH = 256
    }

    jvmOverloads public constructor(context: Context, attrs: AttributeSet? = null) : this(context, null, 0) {
    }

    var outerPathString: String by Delegates.observable("", {
        propertyMetadata: PropertyMetadata, previous: String, current: String ->
        animatePath(propertyMetadata.name, previous, current)
    })

    var innerPathString: String by Delegates.observable("", {
        propertyMetadata: PropertyMetadata, previous: String, current: String ->
        animatePath(propertyMetadata.name, previous, current)
    })

    var medianPathString: String by Delegates.observable("", {
        propertyMetadata: PropertyMetadata, previous: String, current: String ->
        animatePath(propertyMetadata.name, previous, current)
    })

    private fun animatePath(name: String, previous: String, current: String) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val outerPaint: Paint by Delegates.lazy { initializePaint(R.color.primary_dark_material_light) }
    private val innerPaint: Paint by Delegates.lazy { initializePaint(R.color.primary_dark_material_dark) }
    private val medianPaint: Paint by Delegates.lazy { initializePaint(R.color.primary_text_default_material_dark, stroke=true) }
    private val cornerEffect by Delegates.lazy { CornerPathEffect(dp2px(10)) }

    private fun dp2px(dp: Int): Float {
        val m = resources?.getDisplayMetrics()
        return if (m != null) dp * m.densityDpi / 160f else Float.NaN
    }

    private fun initializePaint(colorResource: Int, stroke: Boolean = false, strokeWidth: Float = dp2px(3)): Paint {
        val paint = Paint()
        paint.setColor(resources!!getColor(colorResource))
        if (stroke) {
            paint.setStyle(Paint.Style.STROKE)
            paint.setStrokeWidth(strokeWidth)
        } else
            paint.setStyle(Paint.Style.FILL)
        paint.setPathEffect(cornerEffect)
        return paint
    }

}
