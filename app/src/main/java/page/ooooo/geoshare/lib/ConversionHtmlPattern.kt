package page.ooooo.geoshare.lib

import kotlin.collections.firstNotNullOfOrNull
import kotlin.collections.flatten
import kotlin.collections.mapNotNull

interface ConversionHtmlPattern<T : ConversionRegex> {
    fun find(content: String): List<T>?
}

class ConversionHtmlContentPattern<T : ConversionRegex>(val conversionRegex: T) : ConversionHtmlPattern<T> {
    override fun find(content: String): List<T>? =
        conversionRegex.takeIf { it.find(content) }?.let { listOf(it) }
}

abstract class ConversionGroupHtmlPattern<T : ConversionRegex> : ConversionHtmlPattern<T> {
    val children: MutableList<ConversionHtmlPattern<T>> = mutableListOf()

    fun all(init: ConversionAllHtmlPattern<T>.() -> Unit) =
        initMatcher(ConversionAllHtmlPattern(), init)

    fun first(init: ConversionFirstHtmlPattern<T>.() -> Unit) =
        initMatcher(ConversionFirstHtmlPattern(), init)

    fun content(conversionRegex: T) =
        initMatcher(ConversionHtmlContentPattern(conversionRegex))

    private fun <U : ConversionHtmlPattern<T>> initMatcher(conversionPattern: U, init: U.() -> Unit = {}): U {
        conversionPattern.init()
        children.add(conversionPattern)
        return conversionPattern
    }
}

class ConversionAllHtmlPattern<T : ConversionRegex> : ConversionGroupHtmlPattern<T>() {
    override fun find(content: String): List<T>? =
        children.mapNotNull { it.find(content) }.takeIf { it.size == children.size }?.flatten()
}

class ConversionFirstHtmlPattern<T : ConversionRegex> : ConversionGroupHtmlPattern<T>() {
    override fun find(content: String): List<T>? =
        children.firstNotNullOfOrNull { it.find(content) }
}

fun <T : ConversionRegex> htmlPattern(init: ConversionFirstHtmlPattern<T>.() -> Unit): ConversionFirstHtmlPattern<T> {
    val conversionPattern = ConversionFirstHtmlPattern<T>()
    conversionPattern.init()
    return conversionPattern
}
