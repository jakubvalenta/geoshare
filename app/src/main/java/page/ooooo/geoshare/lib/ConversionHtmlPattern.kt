package page.ooooo.geoshare.lib

import com.google.re2j.Pattern

abstract class ConversionHtmlPattern {
    val lat = """[\+ ]?(?P<lat>-?\d{1,2}(\.\d{1,16})?)"""
    val lon = """[\+ ]?(?P<lon>-?\d{1,3}(\.\d{1,16})?)"""

    abstract fun matches(content: String): List<ConversionMatcher>?
}

class ConversionHtmlContentPattern(
    contentRegex: String,
    val transform: TransformFunc = null,
) : ConversionHtmlPattern() {
    val contentPattern: Pattern = Pattern.compile(contentRegex, Pattern.DOTALL)

    override fun matches(content: String): List<ConversionMatcher>? =
        ConversionMatcher(contentPattern, content, transform).takeIf { it.matches() }?.let { listOf(it) }
}

abstract class ConversionGroupHtmlPattern : ConversionHtmlPattern() {
    val children: MutableList<ConversionHtmlPattern> = mutableListOf()

    fun all(init: ConversionAllHtmlPattern.() -> Unit) = initMatcher(ConversionAllHtmlPattern(), init)

    fun first(init: ConversionFirstHtmlPattern.() -> Unit) = initMatcher(ConversionFirstHtmlPattern(), init)

    fun html(contentRegex: String, transform: TransformFunc = null) =
        initMatcher(ConversionHtmlContentPattern(contentRegex, transform))

    private fun <T : ConversionHtmlPattern> initMatcher(conversionPattern: T, init: T.() -> Unit = {}): T {
        conversionPattern.init()
        children.add(conversionPattern)
        return conversionPattern
    }
}

class ConversionAllHtmlPattern() : ConversionGroupHtmlPattern() {
    override fun matches(content: String): List<ConversionMatcher>? =
        children.mapNotNull { it.matches(content) }.takeIf { it.size == children.size }?.flatten()
}

class ConversionFirstHtmlPattern() : ConversionGroupHtmlPattern() {
    override fun matches(content: String): List<ConversionMatcher>? =
        children.firstNotNullOfOrNull { it.matches(content) }
}

fun htmlPattern(init: ConversionFirstHtmlPattern.() -> Unit): ConversionFirstHtmlPattern {
    val conversionPattern = ConversionFirstHtmlPattern()
    conversionPattern.init()
    return conversionPattern
}
