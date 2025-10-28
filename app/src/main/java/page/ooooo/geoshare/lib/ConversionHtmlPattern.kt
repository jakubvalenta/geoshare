package page.ooooo.geoshare.lib

interface ConversionHtmlPattern<M> {
    fun find(content: String): List<M>?
}

class ConversionHtmlContentPattern<M, R : ConversionRegex<M>>(val conversionRegex: R) : ConversionHtmlPattern<M> {
    override fun find(content: String): List<M>? = conversionRegex.find(content)?.let { listOf(it) }
}

abstract class ConversionGroupHtmlPattern<M, R : ConversionRegex<M>> : ConversionHtmlPattern<M> {
    val children: MutableList<ConversionHtmlPattern<M>> = mutableListOf()

    fun all(init: ConversionAllHtmlPattern<M, R>.() -> Unit) =
        initMatcher(ConversionAllHtmlPattern(), init)

    fun first(init: ConversionFirstHtmlPattern<M, R>.() -> Unit) =
        initMatcher(ConversionFirstHtmlPattern(), init)

    fun content(conversionRegex: R) = initMatcher(ConversionHtmlContentPattern(conversionRegex))

    private fun <P : ConversionHtmlPattern<M>> initMatcher(conversionPattern: P, init: P.() -> Unit = {}): P {
        conversionPattern.init()
        children.add(conversionPattern)
        return conversionPattern
    }
}

class ConversionAllHtmlPattern<M, R : ConversionRegex<M>> : ConversionGroupHtmlPattern<M, R>() {
    override fun find(content: String): List<M>? =
        children.mapNotNull { it.find(content) }.takeIf { it.size == children.size }?.flatten()
}

class ConversionFirstHtmlPattern<M, R : ConversionRegex<M>> : ConversionGroupHtmlPattern<M, R>() {
    override fun find(content: String): List<M>? =
        children.firstNotNullOfOrNull { it.find(content) }
}

fun <M, R : ConversionRegex<M>> htmlPattern(init: ConversionFirstHtmlPattern<M, R>.() -> Unit): ConversionFirstHtmlPattern<M, R> {
    val conversionPattern = ConversionFirstHtmlPattern<M, R>()
    conversionPattern.init()
    return conversionPattern
}
