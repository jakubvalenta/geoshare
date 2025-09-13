package page.ooooo.geoshare.lib

import com.google.re2j.Pattern

abstract class ConversionHtmlPattern() {
    val lat = """[\+ ]?(?P<lat>-?\d{1,2}(\.\d{1,16})?)"""
    val lon = """[\+ ]?(?P<lon>-?\d{1,3}(\.\d{1,16})?)"""

    val children: MutableList<ConversionHtmlPattern> = mutableListOf()

    abstract fun matches(content: String): List<ConversionMatcher>?

    protected fun <T : ConversionHtmlPattern> initMatcher(conversionPattern: T, init: T.() -> Unit = {}): T {
        conversionPattern.init()
        children.add(conversionPattern)
        return conversionPattern
    }
}

class ConversionHtmlContentPattern(
    contentRegex: String,
    val transform: TransformFunc = null,
) : ConversionHtmlPattern() {
    val contentPattern: Pattern = Pattern.compile(contentRegex, Pattern.DOTALL)

    override fun matches(content: String): List<ConversionMatcher>? =
        ConversionMatcher(contentPattern, content, transform).takeIf { it.matches() }?.let { listOf(it) }
}

class ConversionAllHtmlPattern() : ConversionHtmlPattern() {
    fun html(contentRegex: String, transform: TransformFunc = null) =
        initMatcher(ConversionHtmlContentPattern(contentRegex, transform))

    override fun matches(content: String): List<ConversionMatcher>? =
        children.mapNotNull { it.matches(content) }.takeIf { it.size == children.size }?.flatten()
}

class ConversionFirstHtmlPattern() : ConversionHtmlPattern() {
    fun html(contentRegex: String, transform: TransformFunc = null) =
        initMatcher(ConversionHtmlContentPattern(contentRegex, transform))

    override fun matches(content: String): List<ConversionMatcher>? =
        children.firstNotNullOfOrNull { it.matches(content) }
}

fun allHtmlPattern(init: ConversionAllHtmlPattern.() -> Unit): ConversionAllHtmlPattern {
    val conversionPattern = ConversionAllHtmlPattern()
    conversionPattern.init()
    return conversionPattern
}

fun firstHtmlPattern(init: ConversionFirstHtmlPattern.() -> Unit): ConversionFirstHtmlPattern {
    val conversionPattern = ConversionFirstHtmlPattern()
    conversionPattern.init()
    return conversionPattern
}
