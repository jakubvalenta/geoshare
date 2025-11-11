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

infix fun String.matches(pattern: Pattern): Matcher? =
    pattern.matcher(this).takeIf { it.matches() }

infix fun String.matches(regex: String): Matcher? =
    this matches Pattern.compile(regex)

infix fun String.find(pattern: Pattern): Matcher? =
    pattern.matcher(this).takeIf { it.find() }

infix fun String.find(regex: String): Matcher? =
    this find Pattern.compile(regex)

infix fun String.findAll(pattern: Pattern): Sequence<Matcher> =
    pattern.matcher(this).let { m -> generateSequence { m.takeIf { it.find() } } }

infix fun String.findAll(regex: String): Sequence<Matcher> =
    this findAll Pattern.compile(regex)
