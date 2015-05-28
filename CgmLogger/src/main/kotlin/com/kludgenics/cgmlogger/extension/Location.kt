package com.kludgenics.cgmlogger.extension

import android.graphics.Path
import android.location.Location
import android.util.Log

/**
 * Calculates the error function assuming a normal distribution with mean 0 and standard deviation 1
 */
private fun errorFunction(x: Double) : Double {
    if (x < 0)
        return -errorFunction(-x)
    else
        return 1.0 - Math.pow((1.0 / (1.0 + .278393 * x + 0.230389 * x * x + .000972 * x * x * x + .078108 * x * x * x * x)), 4.0)
}

private fun complimentaryErrorFunction(x: Double): Double {
    return 1 - errorFunction(x)
}

private fun cumulativeDensityFunction(x: Double, mean: Double, sigma: Double): Double {
    return .5 * (complimentaryErrorFunction((mean-x)/(sigma * Math.sqrt(2.0))))
}

public fun Location.probabilityWithin(neighbor: Location) : Double {
    val sigma = this.getAccuracy().toDouble()
    val distance = this.distanceTo(neighbor).toDouble()
    Log.d("moo", "$sigma $distance ${cumulativeDensityFunction((-1.0 * distance).toDouble(), 0.0, sigma.toDouble())}" )
    val p = Path()
    return 1 - (cumulativeDensityFunction(distance, 0.0, sigma) - cumulativeDensityFunction(-distance, 0.0, sigma))
}