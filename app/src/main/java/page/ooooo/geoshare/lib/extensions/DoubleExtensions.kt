package page.ooooo.geoshare.lib.extensions

import java.math.RoundingMode
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.truncate

fun Double.toDegMinSec(): Triple<Int, Int, Double> {
    val sig = sign(this).toInt()
    val degAbs = abs(this)
    val degInt = truncate(degAbs).toInt()
    val min = abs(degAbs - degInt) * 60
    val minInt = truncate(min).toInt()
    val sec = (min - minInt) * 60
    return Triple(sig * degInt, minInt, sec)
}

fun Double.toScale(scale: Int) = this.toBigDecimal().setScale(scale, RoundingMode.HALF_UP).toDouble()

fun Double.toTrimmedString(): String = if (this % 1.0 == 0.0) {
    this.roundToInt().toString()
} else {
    String.format(Locale.ROOT, "%.15f", this).trimEnd('0').trimEnd('.')
}
