package com.kludgenics.justgivemeachart

import android.graphics.Paint
import android.view.View
import org.jetbrains.annotations.NotNull

/**
 * Created by matthias on 4/22/16.
 */
abstract class Legend(@NotNull primaryPaint: Paint? = Paint(), secondaryPaint: Paint? = Paint(), vararg var inks: Ink): Chartjunk {
    override var visibility: Int = Ink.Visibility.VISIBLE
    override var description: String? = null
    override var primaryPaint = primaryPaint
}