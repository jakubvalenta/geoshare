package page.ooooo.geoshare.lib

import com.google.re2j.Pattern
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

abstract class ConversionUrlPattern() {
    open val lat = """[\+ ]?(?P<lat>-?\d{1,2}(\.\d{1,16})?)"""
    open val lon = """[\+ ]?(?P<lon>-?\d{1,3}(\.\d{1,16})?)"""
    open val z = """(?P<z>\d{1,2}(\.\d{1,16})?)"""
    open val q = """(?P<q>.+)"""
    open val sanitizeZoom = { name: String, value: String? ->
        if (name == "z") {
            value?.let { max(1, min(21, it.toDouble().roundToInt())).toString() }
        } else {
            value
        }
    }

    val children: MutableList<ConversionUrlPattern> = mutableListOf()

    abstract fun matches(
        urlHost: String,
        urlPath: String,
        urlQueryParams: Map<String, String>,
    ): List<ConversionMatcher>?

    protected fun <T : ConversionUrlPattern> initMatcher(conversionPattern: T, init: T.() -> Unit = {}): T {
        conversionPattern.init()
        children.add(conversionPattern)
        return conversionPattern
    }
}

class ConversionHostUrlPattern(hostRegex: String) : ConversionUrlPattern() {
    val hostPattern: Pattern = Pattern.compile(hostRegex)

    override fun matches(
        urlHost: String,
        urlPath: String,
        urlQueryParams: Map<String, String>,
    ): List<ConversionMatcher>? =
        ConversionMatcher(hostPattern, urlHost).takeIf { it.matches() }?.let { listOf(it) }
}

class ConversionPathUrlPattern(
    pathRegex: String,
    val transform: ((String, String?) -> String?)? = null,
) : ConversionUrlPattern() {
    val pathPattern: Pattern = Pattern.compile(pathRegex)

    override fun matches(
        urlHost: String,
        urlPath: String,
        urlQueryParams: Map<String, String>,
    ): List<ConversionMatcher>? =
        ConversionMatcher(pathPattern, urlPath, transform).takeIf { it.matches() }?.let { listOf(it) }
}

class ConversionQueryParamUrlPattern(
    val name: String,
    valueRegex: String,
    val transform: ((String, String?) -> String?)? = null,
) : ConversionUrlPattern() {
    val valuePattern: Pattern = Pattern.compile(valueRegex)

    override fun matches(
        urlHost: String,
        urlPath: String,
        urlQueryParams: Map<String, String>,
    ): List<ConversionMatcher>? =
        urlQueryParams[name]?.let { value -> ConversionMatcher(valuePattern, value, transform).takeIf { it.matches() } }
            ?.let { listOf(it) }
}

class ConversionOptionalUrlPattern : ConversionUrlPattern() {
    fun first(init: ConversionFirstUrlPattern.() -> Unit) = initMatcher(ConversionFirstUrlPattern(), init)
    fun query(name: String, valueRegex: String, transform: ((String, String?) -> String?)? = null) =
        initMatcher(ConversionQueryParamUrlPattern(name, valueRegex, transform))

    override fun matches(
        urlHost: String,
        urlPath: String,
        urlQueryParams: Map<String, String>,
    ): List<ConversionMatcher> =
        children.mapNotNull { it.matches(urlHost, urlPath, urlQueryParams) }.flatten()
}

class ConversionAllUrlPattern() : ConversionUrlPattern() {
    fun first(init: ConversionFirstUrlPattern.() -> Unit) = initMatcher(ConversionFirstUrlPattern(), init)
    fun optional(init: ConversionOptionalUrlPattern.() -> Unit) = initMatcher(ConversionOptionalUrlPattern(), init)
    fun host(hostRegex: String) = initMatcher(ConversionHostUrlPattern(hostRegex))
    fun path(pathRegex: String, transform: ((String, String?) -> String?)? = null) =
        initMatcher(ConversionPathUrlPattern(pathRegex, transform))

    fun query(name: String, valueRegex: String, transform: ((String, String?) -> String?)? = null) =
        initMatcher(ConversionQueryParamUrlPattern(name, valueRegex, transform))

    override fun matches(
        urlHost: String,
        urlPath: String,
        urlQueryParams: Map<String, String>,
    ): List<ConversionMatcher>? =
        children.mapNotNull { it.matches(urlHost, urlPath, urlQueryParams) }.takeIf { it.size == children.size }
            ?.flatten()
}

class ConversionFirstUrlPattern() : ConversionUrlPattern() {
    fun all(init: ConversionAllUrlPattern.() -> Unit) = initMatcher(ConversionAllUrlPattern(), init)
    fun path(pathRegex: String, transform: ((String, String?) -> String?)? = null) =
        initMatcher(ConversionPathUrlPattern(pathRegex, transform))

    fun query(name: String, valueRegex: String, transform: ((String, String?) -> String?)? = null) =
        initMatcher(ConversionQueryParamUrlPattern(name, valueRegex, transform))

    override fun matches(
        urlHost: String,
        urlPath: String,
        urlQueryParams: Map<String, String>,
    ): List<ConversionMatcher>? =
        children.firstNotNullOfOrNull { it.matches(urlHost, urlPath, urlQueryParams) }
}

fun allUrlPattern(init: ConversionAllUrlPattern.() -> Unit): ConversionAllUrlPattern {
    val conversionPattern = ConversionAllUrlPattern()
    conversionPattern.init()
    return conversionPattern
}
