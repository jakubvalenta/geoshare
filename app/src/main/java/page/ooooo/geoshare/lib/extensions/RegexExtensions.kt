package page.ooooo.geoshare.lib.extensions

import com.google.re2j.Matcher
import com.google.re2j.Pattern

fun Matcher.groupOrNull(): String? = try {
    this.group()
} catch (_: IllegalArgumentException) {
    null
}

fun Matcher.groupOrNull(name: String): String? = try {
    this.group(name)
} catch (_: IllegalArgumentException) {
    null
}

infix fun String.matches(regex: String): Matcher? =
    Pattern.compile(regex).matcher(this)?.takeIf { it.matches() }

infix fun String.find(regex: String): Matcher? =
    Pattern.compile(regex).matcher(this)?.takeIf { it.find() }
