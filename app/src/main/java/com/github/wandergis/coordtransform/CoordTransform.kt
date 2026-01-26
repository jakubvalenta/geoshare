package com.github.wandergis.coordtransform

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object CoordTransform {
    const val X_PI = PI * 3000.0 / 180.0

    fun bd09toGCJ02(lat: Double, lon: Double): Pair<Double, Double> {
        val x = lon - 0.0065
        val y = lat - 0.006
        val z = sqrt(x * x + y * y) - 0.00002 * sin(y * X_PI)
        val theta = atan2(y, x) - 0.000003 * cos(x * X_PI)
        return z * sin(theta) to z * cos(theta)
    }
}
