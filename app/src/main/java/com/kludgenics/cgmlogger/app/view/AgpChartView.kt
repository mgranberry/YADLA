package com.kludgenics.cgmlogger.app.view

import android.content.Context
import android.util.AttributeSet
import android.view.View

public class AgpChartView(context: Context, attrs: AttributeSet?, defStyle: Int) : View(context, attrs, defStyle) {
    companion object {
        val MODEL_HEIGHT = 256
        val MODEL_WIDTH = 256
    }

    jvmOverloads public constructor(context: Context, attrs: AttributeSet? = null) : this(context, null, 0) {
    }
}
