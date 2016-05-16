package com.kludgenics.justgivemeachart

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.properties.Delegates

/**
 * Created by matthias on 5/3/16.
 */
class DiscretePointLine<T, U>(override var description: String?, override var primaryPaint: Paint?, override var viewport: RectF) : Line<T, U> {
    override fun baselineValueAt(index: Float): Float {
        throw UnsupportedOperationException()
    }

    override var visibility: Int
        get() = throw UnsupportedOperationException()
        set(value) {
        }

    override var valueAdapter: ValueAdapter<T, U>? by Delegates.observable<ValueAdapter<T, U>?>(null) {
        property, oldValue, newValue ->
        invalidate()
    }

    private val valuePath = Path()

    fun updatePath() {
        valuePath.rewind()
        valueAdapter
    }

    override fun invalidate() {}

    override fun onDraw(canvas: Canvas) {
        throw UnsupportedOperationException()
    }

}