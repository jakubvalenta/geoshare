package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.Position

abstract class HtmlPattern() {
    val children: MutableList<HtmlPattern> = mutableListOf()

    abstract fun matches(text: String): Position?

    protected fun <T : HtmlPattern> initMatcher(htmlPattern: T, init: T.() -> Unit = {}): T {
        htmlPattern.init()
        children.add(htmlPattern)
        return htmlPattern
    }
}

class HtmlTextPattern(
    val pattern: Pattern,
) : HtmlPattern() {
    override fun matches(text: String): Position? =
        pattern.matcher(text)?.takeIf { it.matches() }?.let { Position.fromMatcher(it) }
}

class HtmlAllPattern() : HtmlPattern() {
    fun text(pattern: Pattern) = initMatcher(HtmlTextPattern(pattern))

    override fun matches(text: String): Position? =
        children.mapNotNull { it.matches(text) }.takeIf { it.isNotEmpty() }
            ?.reduceRight { sum, element -> sum.union(element) }
}

fun html(init: HtmlAllPattern.() -> Unit): HtmlAllPattern {
    val htmlPattern = HtmlAllPattern()
    htmlPattern.init()
    return htmlPattern
}
