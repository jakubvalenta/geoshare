package page.ooooo.geoshare.lib

import com.google.re2j.Matcher
import com.google.re2j.Pattern

typealias TransformFunc = (ConversionMatcher.(name: String, value: String?) -> String?)?

data class ConversionMatcher(
    val matcher: Matcher,
    val transform: TransformFunc = null,
) {
    constructor(pattern: Pattern, input: String, transform: TransformFunc = null) :
            this(pattern.matcher(input), transform)

    fun matches(): Boolean = matcher.matches()

    fun get(name: String): String? = groupOrNull(matcher, name).let { value ->
        if (transform != null) {
            this@ConversionMatcher.transform(name, value)
        } else {
            value
        }
    }

    fun groupOrNull(m: Matcher, name: String): String? = try {
        m.group(name)
    } catch (_: IllegalArgumentException) {
        null
    }
}

fun List<ConversionMatcher>.groupOrNull(name: String): String? = this.lastNotNullOrNull { it.get(name) }
