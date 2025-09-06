package page.ooooo.geoshare.lib

import com.google.re2j.Pattern

abstract class ConversionHtmlPattern() {
    val lat = """\+?(?P<lat>-?\d{1,2}(\.\d{1,16})?)"""
    val lon = """\+?(?P<lon>-?\d{1,3}(\.\d{1,16})?)"""

    val children: MutableList<ConversionHtmlPattern> = mutableListOf()

    abstract fun matches(html: String): ConversionMatcher?

    protected fun <T : ConversionHtmlPattern> initMatcher(conversionPattern: T, init: T.() -> Unit = {}): T {
        conversionPattern.init()
        children.add(conversionPattern)
        return conversionPattern
    }
}

class ConversionHtmlUrlPattern(htmlRegex: String) : ConversionHtmlPattern() {
    val htmlPattern: Pattern = Pattern.compile(htmlRegex)

    override fun matches(html: String): ConversionMatcher? =
        htmlPattern.matcher(html)?.takeIf { it.matches() }?.let { ConversionMatcher(listOf(it)) }
}

class ConversionAllHtmlPattern() : ConversionHtmlPattern() {
    fun html(htmlRegex: String) = initMatcher(ConversionHtmlUrlPattern(htmlRegex))

    override fun matches(html: String): ConversionMatcher? =
        children.mapNotNull { it.matches(html) }.takeIf { it.size == children.size }
            ?.let { ConversionMatcher.fromConversionMatchers(it) }
}

fun allHtmlPattern(init: ConversionAllHtmlPattern.() -> Unit): ConversionAllHtmlPattern {
    val conversionPattern = ConversionAllHtmlPattern()
    conversionPattern.init()
    return conversionPattern
}
