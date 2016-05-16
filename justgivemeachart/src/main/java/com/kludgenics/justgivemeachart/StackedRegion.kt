package com.kludgenics.justgivemeachart

class StackedRegion<T, U>(private val base: Region<T, U>, private val region: Region<T, U>): StackedLine<T, U>(base, region), Region<T, U> by region