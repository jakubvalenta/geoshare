package page.ooooo.geoshare.lib

import java.math.RoundingMode

fun Double.toScale(scale: Int) = this.toBigDecimal().setScale(scale, RoundingMode.HALF_UP).toDouble()
