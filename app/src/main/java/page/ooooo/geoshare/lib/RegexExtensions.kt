package page.ooooo.geoshare.lib

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

fun Pattern.matcherIfMatches(input: String): Matcher? = this.matcher(input)?.takeIf { it.matches() }

fun Pattern.matcherIfFind(input: String): Matcher? = this.matcher(input)?.takeIf { it.find() }

infix fun String.matcherIfMatches(regex: String): Matcher? = Pattern.compile(regex).matcherIfMatches(this)

infix fun String.matcherIfFind(regex: String): Matcher? = Pattern.compile(regex).matcherIfFind(this)
