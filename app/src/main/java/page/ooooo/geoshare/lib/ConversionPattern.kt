package page.ooooo.geoshare.lib

import com.google.re2j.Matcher
import com.google.re2j.Pattern

abstract class ConversionPattern<T> {
    open fun find(html: String): List<T>? = null
    open fun matches(uri: Uri): List<T>? = null
}

class ConversionHostPattern<T>(
    val regex: String,
    val getMatch: (matcher: Matcher) -> T,
) : ConversionPattern<T>() {

    override fun matches(uri: Uri): List<T>? = Pattern.compile(regex).matcherIfMatches(uri.host)
        ?.let { listOf(getMatch(it)) }
}

class ConversionPathPattern<T>(
    val regex: String,
    val getMatch: (matcher: Matcher) -> T,
) : ConversionPattern<T>() {

    override fun matches(uri: Uri): List<T>? = Pattern.compile(regex).matcherIfMatches(uri.path)
        ?.let { listOf(getMatch(it)) }
}

class ConversionQueryParamPattern<T>(
    val name: String,
    val regex: String,
    val getMatch: (matcher: Matcher) -> T,
) : ConversionPattern<T>() {

    override fun matches(uri: Uri): List<T>? =
        uri.queryParams[name]?.let { value -> Pattern.compile(regex).matcherIfMatches(value) }
            ?.let { listOf(getMatch(it)) }
}

class ConversionFragmentPattern<T>(
    val regex: String,
    val getMatch: (matcher: Matcher) -> T,
) : ConversionPattern<T>() {

    override fun matches(uri: Uri): List<T>? = Pattern.compile(regex).matcherIfMatches(uri.fragment)
        ?.let { listOf(getMatch(it)) }
}

class ConversionHtmlPattern<T>(
    val regex: String,
    val getMatch: (matcher: Matcher) -> T,
) : ConversionPattern<T>() {

    override fun find(html: String): List<T>? = Pattern.compile(regex).matcherIfFind(html)
        ?.let { listOf(getMatch(it)) }
}

abstract class ConversionGroupPattern<T> : ConversionPattern<T>() {
    val children: MutableList<ConversionPattern<T>> = mutableListOf()

    fun all(init: ConversionAllPattern<T>.() -> Unit) = initMatcher(ConversionAllPattern(), init)

    fun first(init: ConversionFirstPattern<T>.() -> Unit) = initMatcher(ConversionFirstPattern(), init)

    fun optional(init: ConversionOptionalPattern<T>.() -> Unit) =
        initMatcher(ConversionOptionalPattern(), init)

    fun host(regex: String, getMatch: (matcher: Matcher) -> T) =
        initMatcher(ConversionHostPattern(regex, getMatch))

    fun path(regex: String, getMatch: (matcher: Matcher) -> T) =
        initMatcher(ConversionPathPattern(regex, getMatch))

    fun query(name: String, regex: String, getMatch: (matcher: Matcher) -> T) =
        initMatcher(ConversionQueryParamPattern(name, regex, getMatch))

    fun fragment(regex: String, getMatch: (matcher: Matcher) -> T) =
        initMatcher(ConversionFragmentPattern(regex, getMatch))

    fun html(regex: String, getMatch: (matcher: Matcher) -> T) =
        initMatcher(ConversionHtmlPattern(regex, getMatch))

    private fun <U : ConversionPattern<T>> initMatcher(conversionPattern: U, init: U.() -> Unit = {}): U {
        conversionPattern.init()
        children.add(conversionPattern)
        return conversionPattern
    }
}

class ConversionOptionalPattern<T> : ConversionGroupPattern<T>() {
    override fun find(html: String): List<T> = children.mapNotNull { it.find(html) }.flatten()
    override fun matches(uri: Uri): List<T> = children.mapNotNull { it.matches(uri) }.flatten()
}

class ConversionAllPattern<T> : ConversionGroupPattern<T>() {
    override fun find(html: String): List<T>? =
        children.mapNotNull { it.find(html) }.takeIf { it.size == children.size }?.flatten()

    override fun matches(uri: Uri): List<T>? =
        children.mapNotNull { it.matches(uri) }.takeIf { it.size == children.size }?.flatten()
}

class ConversionFirstPattern<T> : ConversionGroupPattern<T>() {
    override fun find(html: String): List<T>? = children.firstNotNullOfOrNull { it.find(html) }
    override fun matches(uri: Uri): List<T>? = children.firstNotNullOfOrNull { it.matches(uri) }
}

fun <T> conversionPattern(init: ConversionFirstPattern<T>.() -> Unit): ConversionFirstPattern<T> {
    val conversionPattern = ConversionFirstPattern<T>()
    conversionPattern.init()
    return conversionPattern
}
