package page.ooooo.geoshare.lib

import com.google.re2j.Matcher
import com.google.re2j.Pattern

data class ConversionMatcher(
    val matcher: Matcher,
    val transform: ((name: String, value: String?) -> String?)? = null,
) {
    constructor(
        pattern: Pattern,
        input: String,
        transform: ((name: String, value: String?) -> String?)? = null,
    ) : this(pattern.matcher(input), transform)

    fun matches(): Boolean = matcher.matches()

    fun groupOrNull(name: String): String? {
        val value = try {
            matcher.group(name)
        } catch (_: IllegalArgumentException) {
            null
        }
        return if (transform != null) {
            transform(name, value)
        } else {
            value
        }
    }
}

fun List<ConversionMatcher>.groupOrNull(name: String): String? = this.lastNotNullOrNull { it.groupOrNull(name) }
