package page.ooooo.geoshare.lib

import java.math.RoundingMode

fun Int.forEachBinaryDigit(bitCount: Int, action: Int.(bit: Int) -> Unit) {
    for (i in bitCount - 1 downTo 0) {
        val bit = (this shr i) and 1
        this.action(bit)
    }
}

fun Int.isOdd(): Boolean = this and 1 == 0

fun Double.toScale(scale: Int) = this.toBigDecimal().setScale(scale, RoundingMode.HALF_UP).toDouble()
