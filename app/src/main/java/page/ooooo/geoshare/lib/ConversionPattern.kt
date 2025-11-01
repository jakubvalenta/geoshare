package page.ooooo.geoshare.lib

import com.google.re2j.Matcher

abstract class ConversionPattern<T> {
    open fun matches(uri: Uri): List<T>? = null
    open fun matches(html: String): List<T>? = null
}

class ConversionUriPattern<T>(
    val condition: Uri.() -> Matcher?,
    val result: (matcher: Matcher) -> T,
) : ConversionPattern<T>() {
    class Builder<T>(
        val condition: Uri.() -> Matcher?,
        val block: (conversionUriPattern: ConversionUriPattern<T>) -> Unit,
    ) {
        infix fun doReturn(result: (matcher: Matcher) -> T) = block(ConversionUriPattern(condition, result))
    }

    override fun matches(uri: Uri): List<T>? = uri.condition()?.let { listOf(result(it)) }
}

class ConversionHtmlPattern<T>(
    val condition: String.() -> Matcher?,
    val result: (matcher: Matcher) -> T,
) : ConversionPattern<T>() {
    class Builder<T>(
        val condition: String.() -> Matcher?,
        val block: (conversionHtmlPattern: ConversionHtmlPattern<T>) -> Unit,
    ) {
        infix fun doReturn(result: (matcher: Matcher) -> T) =
            block(ConversionHtmlPattern(condition, result))
    }

    override fun matches(html: String): List<T>? = html.condition()?.let { listOf(result(it)) }
}

abstract class ConversionGroupPattern<T> : ConversionPattern<T>() {
    val children: MutableList<ConversionPattern<T>> = mutableListOf()

    fun all(init: ConversionAllPattern<T>.() -> Unit) = initMatcher(ConversionAllPattern(), init)

    fun first(init: ConversionFirstPattern<T>.() -> Unit) = initMatcher(ConversionFirstPattern(), init)

    fun onUri(condition: Uri.() -> Matcher?) = ConversionUriPattern.Builder(condition) { initMatcher(it) }

    fun onHtml(condition: String.() -> Matcher?) = ConversionHtmlPattern.Builder(condition) { initMatcher(it) }

    fun optional(init: ConversionOptionalPattern<T>.() -> Unit) = initMatcher(ConversionOptionalPattern(), init)

    private fun <U : ConversionPattern<T>> initMatcher(conversionPattern: U, init: U.() -> Unit = {}): U {
        conversionPattern.init()
        children.add(conversionPattern)
        return conversionPattern
    }
}

class ConversionOptionalPattern<T> : ConversionGroupPattern<T>() {
    override fun matches(uri: Uri): List<T> = children.mapNotNull { it.matches(uri) }.flatten()
    override fun matches(html: String): List<T> = children.mapNotNull { it.matches(html) }.flatten()
}

class ConversionAllPattern<T> : ConversionGroupPattern<T>() {
    override fun matches(uri: Uri): List<T>? =
        children.mapNotNull { it.matches(uri) }.takeIf { it.size == children.size }?.flatten()

    override fun matches(html: String): List<T>? =
        children.mapNotNull { it.matches(html) }.takeIf { it.size == children.size }?.flatten()
}

class ConversionFirstPattern<T> : ConversionGroupPattern<T>() {
    override fun matches(uri: Uri): List<T>? = children.firstNotNullOfOrNull { it.matches(uri) }
    override fun matches(html: String): List<T>? = children.firstNotNullOfOrNull { it.matches(html) }
}

fun <T> conversionPattern(init: ConversionFirstPattern<T>.() -> Unit): ConversionFirstPattern<T> {
    val conversionPattern = ConversionFirstPattern<T>()
    conversionPattern.init()
    return conversionPattern
}
