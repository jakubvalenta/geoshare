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

infix fun Pattern.find(input: String?): Matcher? = input?.let { input ->
    this.matcher(input).takeIf { it.find() }
}

infix fun Pattern.findAll(input: String?): Sequence<Matcher> = input?.let { input ->
    this.matcher(input).let { m -> generateSequence { m.takeIf { it.find() } } }
}.orEmpty()

infix fun Pattern.match(input: String?): Matcher? = input?.let { input ->
    this.matcher(input).takeIf { it.matches() }
}

infix fun String.find(input: String?): Matcher? = input?.let { input ->
    Pattern.compile(this) find input
}

infix fun String.findAll(input: String?): Sequence<Matcher> = input?.let { input ->
    Pattern.compile(this) findAll input
}.orEmpty()

infix fun String.match(input: String?): Matcher? = input?.let { input ->
    Pattern.compile(this) match input
}
