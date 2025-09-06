package page.ooooo.geoshare.lib

import com.google.re2j.Pattern

abstract class ConversionUrlPattern() {
    open val lat = """\+?(?P<lat>-?\d{1,2}(\.\d{1,16})?)"""
    open val lon = """\+?(?P<lon>-?\d{1,3}(\.\d{1,16})?)"""
    open val z = """(?P<z>\d{1,2}(\.\d{1,16})?)"""
    open val q = """(?P<q>.+)"""

    val children: MutableList<ConversionUrlPattern> = mutableListOf()

    abstract fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): ConversionMatcher?

    protected fun <T : ConversionUrlPattern> initMatcher(conversionPattern: T, init: T.() -> Unit = {}): T {
        conversionPattern.init()
        children.add(conversionPattern)
        return conversionPattern
    }
}

class ConversionHostUrlPattern(hostRegex: String) : ConversionUrlPattern() {
    val hostPattern: Pattern = Pattern.compile(hostRegex)

    override fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): ConversionMatcher? =
        hostPattern.matcher(urlHost)?.takeIf { it.matches() }?.let { ConversionMatcher(listOf(it)) }
}

class ConversionPathUrlPattern(pathRegex: String) : ConversionUrlPattern() {
    override val q = """(?P<q>[^/]+)"""

    val pathPattern: Pattern = Pattern.compile(pathRegex)

    override fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): ConversionMatcher? =
        pathPattern.matcher(urlPath)?.takeIf { it.matches() }?.let { ConversionMatcher(listOf(it)) }
}

class ConversionQueryParamUrlPattern(val name: String, valueRegex: String) : ConversionUrlPattern() {
    val valuePattern: Pattern = Pattern.compile(valueRegex)

    override fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): ConversionMatcher? =
        urlQueryParams[name]?.let { valuePattern.matcher(it) }?.takeIf { it.matches() }
            ?.let { ConversionMatcher(listOf(it)) }
}

class ConversionAllUrlPattern() : ConversionUrlPattern() {
    fun first(init: ConversionFirstUrlPattern.() -> Unit) = initMatcher(ConversionFirstUrlPattern(), init)
    fun host(hostRegex: String) = initMatcher(ConversionHostUrlPattern(hostRegex))
    fun path(pathRegex: String) = initMatcher(ConversionPathUrlPattern(pathRegex))
    fun query(name: String, valueRegex: String) = initMatcher(ConversionQueryParamUrlPattern(name, valueRegex))

    override fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): ConversionMatcher? =
        children.mapNotNull { it.matches(urlHost, urlPath, urlQueryParams) }.takeIf { it.size == children.size }
            ?.let { ConversionMatcher.fromConversionMatchers(it) }
}

class ConversionFirstUrlPattern() : ConversionUrlPattern() {
    fun all(init: ConversionAllUrlPattern.() -> Unit) = initMatcher(ConversionAllUrlPattern(), init)
    fun path(pathRegex: String) = initMatcher(ConversionPathUrlPattern(pathRegex))
    fun query(name: String, valueRegex: String) = initMatcher(ConversionQueryParamUrlPattern(name, valueRegex))

    override fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): ConversionMatcher? =
        children.firstNotNullOfOrNull { it.matches(urlHost, urlPath, urlQueryParams) }
}

fun allUrlPattern(init: ConversionAllUrlPattern.() -> Unit): ConversionAllUrlPattern {
    val conversionPattern = ConversionAllUrlPattern()
    conversionPattern.init()
    return conversionPattern
}
