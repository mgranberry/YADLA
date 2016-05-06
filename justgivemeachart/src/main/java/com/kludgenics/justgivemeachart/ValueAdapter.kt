package com.kludgenics.justgivemeachart

interface ValueAdapter<Value, Index> {
    data class Point<Value, Index> (val value: Value, val index: Index)
    val points: List<Point<Value, Index>?>
    val scaledPoints: List<Point<Float, Float>?>

    val minValue: Float?
    val maxValue: Float?
    
    val minIndex: Float?
    val maxIndex: Float?

    fun scaledValue(value: Value): Float
    fun scaledIndex(index: Index): Float

    val Value.scaledValue: Float get() = scaledValue(this)
    val Index.scaledIndex: Float get() = scaledIndex(this)

    val Point<Value, Index>.scaled: Point<Float, Float> get() = Point(value.scaledValue, index.scaledIndex)
}