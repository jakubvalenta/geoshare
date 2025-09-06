package page.ooooo.geoshare.lib

import com.google.re2j.Matcher
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class ConversionMatcher(val reversedMatchers: List<Matcher>) {
    companion object {
        fun fromConversionMatchers(conversionMatchers: List<ConversionMatcher>) = ConversionMatcher(
            conversionMatchers.reversed().flatMap { it.reversedMatchers })
    }

    fun toPosition() = Position(
        lat = getGroupOrNull("lat"),
        lon = getGroupOrNull("lon"),
        q = getGroupOrNull("q"),
        z = getGroupOrNull("z")?.let { max(1, min(21, it.toDouble().roundToInt())).toString() },
    )

    fun getGroupOrNull(name: String): String? = reversedMatchers.firstNotNullOfOrNull { m ->
        try {
            m.group(name)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
