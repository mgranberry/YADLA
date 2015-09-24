package com.kludgenics.cgmlogger.app.view

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import kotlin.properties.Delegates

/**
 * Created by matthiasgranberry on 7/18/15.
 */

data class ScaledPaintedPath(val unscaled: Path, val scaleMatrix: Matrix, val paint: Paint) {
    fun invalidate() {
        unscaled.transform(scaleMatrix, scaled)
    }

    val scaled: Path by lazy(LazyThreadSafetyMode.NONE) {
        val p = Path()
        unscaled.transform(scaleMatrix, p)
        p
    }
}