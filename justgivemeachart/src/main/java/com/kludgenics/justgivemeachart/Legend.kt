package com.kludgenics.justgivemeachart

import android.graphics.Paint

/**
 * Created by matthias on 4/22/16.
 */
class Legend(primaryPaint: Paint = Paint(), secondaryPaint: Paint = Paint(), vararg var inks: Ink): Chartjunk {
    override var description: String? = null
    override var primaryPaint = primaryPaint
    override var secondaryPaint = secondaryPaint
}