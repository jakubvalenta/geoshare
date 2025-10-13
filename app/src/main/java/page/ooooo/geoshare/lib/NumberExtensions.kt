package page.ooooo.geoshare.lib

import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.truncate

fun Double.toScale(scale: Int) = this.toBigDecimal().setScale(scale, RoundingMode.HALF_UP).toDouble()

fun Double.toDegMinSec(): Triple<Int, Int, Double> {
    val sig = sign(this).toInt()
    val degAbs = abs(this)
    val degInt = truncate(degAbs).toInt()
    val min = abs(degAbs - degInt) * 60
    val minInt = truncate(min).toInt()
    val sec = (min - minInt) * 60
    return Triple(sig * degInt, minInt, sec)
}
