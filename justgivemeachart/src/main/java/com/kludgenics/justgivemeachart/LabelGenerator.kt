package com.kludgenics.justgivemeachart

/**
 * Created by matthias on 4/26/16.
 */
interface LabelGenerator {
    val firstLabelIndex: Int
    val lastLabelIndex: Int
    val labelPeriod: Int
    val indexScale: Float

    fun getLabelAt(index: Float): String?
    val labeledPoints: List<Float> get() = (firstLabelIndex .. lastLabelIndex step labelPeriod).map { it * indexScale }
}