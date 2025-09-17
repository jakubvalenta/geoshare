package page.ooooo.geoshare.lib

import kotlin.math.roundToInt

fun Double.toTrimmedString(): String = if (this % 1.0 == 0.0) {
    this.roundToInt().toString()
} else {
    this.toString().trimEnd('0').trimEnd('.')
}
