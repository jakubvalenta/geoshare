package page.ooooo.geoshare.lib

import androidx.compose.ui.util.fastForEachReversed
import com.google.re2j.Matcher
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class ConversionMatcher(val matchers: List<Matcher>) {
    companion object {
        fun fromConversionMatchers(other: List<ConversionMatcher>) = ConversionMatcher(other.flatMap { it.matchers })
    }

    fun toPosition() = Position(
        lat = getGroupOrNull("lat"),
        lon = getGroupOrNull("lon"),
        q = getGroupOrNull("q"),
        z = getGroupOrNull("z")?.let { max(1, min(21, it.toDouble().roundToInt())).toString() },
    )

    fun getGroupOrNull(name: String): String? {
        matchers.fastForEachReversed {
            try {
                return@getGroupOrNull it.group(name)
            } catch (_: IllegalArgumentException) {
                // Do nothing
            }
        }
        return null
    }
}
