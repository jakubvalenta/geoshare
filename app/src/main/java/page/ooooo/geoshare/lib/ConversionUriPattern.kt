package page.ooooo.geoshare.lib

import com.google.re2j.Pattern
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

abstract class ConversionUriPattern() {
    open val lat = """[\+ ]?(?P<lat>-?\d{1,2}(\.\d{1,16})?)"""
    open val lon = """[\+ ]?(?P<lon>-?\d{1,3}(\.\d{1,16})?)"""
    open val z = """(?P<z>\d{1,2}(\.\d{1,16})?)"""
    open val q = """(?P<q>.+)"""
    open val sanitizeZoom: TransformFunc = { name, value ->
        if (name == "z") {
            value?.let { max(1, min(21, it.toDouble().roundToInt())).toString() }
        } else {
            value
        }
    }

    val children: MutableList<ConversionUriPattern> = mutableListOf()

    abstract fun matches(uri: Uri): List<ConversionMatcher>?

    protected fun <T : ConversionUriPattern> initMatcher(conversionPattern: T, init: T.() -> Unit = {}): T {
        conversionPattern.init()
        children.add(conversionPattern)
        return conversionPattern
    }
}

class ConversionHostUriPattern(
    hostRegex: String,
    val transform: TransformFunc = null,
) : ConversionUriPattern() {
    val hostPattern: Pattern = Pattern.compile(hostRegex)

    override fun matches(uri: Uri): List<ConversionMatcher>? =
        ConversionMatcher(hostPattern, uri.host, transform).takeIf { it.matches() }?.let { listOf(it) }
}

class ConversionPathUriPattern(
    pathRegex: String,
    val transform: TransformFunc = null,
) : ConversionUriPattern() {
    val pathPattern: Pattern = Pattern.compile(pathRegex)

    override fun matches(uri: Uri): List<ConversionMatcher>? =
        ConversionMatcher(pathPattern, uri.path, transform).takeIf { it.matches() }?.let { listOf(it) }
}

class ConversionQueryParamUriPattern(
    val name: String,
    valueRegex: String,
    val transform: TransformFunc = null,
) : ConversionUriPattern() {
    val valuePattern: Pattern = Pattern.compile(valueRegex)

    override fun matches(uri: Uri): List<ConversionMatcher>? =
        uri.queryParams[name]?.let { value ->
            ConversionMatcher(valuePattern, value, transform).takeIf { it.matches() }
        }?.let { listOf(it) }
}

class ConversionFragmentUriPattern(
    fragmentRegex: String,
    val transform: TransformFunc = null,
) : ConversionUriPattern() {
    val fragmentPattern: Pattern = Pattern.compile(fragmentRegex)

    override fun matches(uri: Uri): List<ConversionMatcher>? =
        ConversionMatcher(fragmentPattern, uri.fragment, transform).takeIf { it.matches() }?.let { listOf(it) }
}

class ConversionOptionalUriPattern : ConversionUriPattern() {
    fun first(init: ConversionFirstUriPattern.() -> Unit) = initMatcher(ConversionFirstUriPattern(), init)

    @Suppress("unused")
    fun host(hostRegex: String, transform: TransformFunc = null) =
        initMatcher(ConversionHostUriPattern(hostRegex, transform))

    @Suppress("unused")
    fun path(pathRegex: String, transform: TransformFunc = null) =
        initMatcher(ConversionPathUriPattern(pathRegex, transform))

    fun query(name: String, valueRegex: String, transform: TransformFunc = null) =
        initMatcher(ConversionQueryParamUriPattern(name, valueRegex, transform))

    @Suppress("unused")
    fun fragment(fragmentRegex: String, transform: TransformFunc = null) =
        initMatcher(ConversionFragmentUriPattern(fragmentRegex, transform))

    override fun matches(uri: Uri): List<ConversionMatcher> =
        children.mapNotNull { it.matches(uri) }.flatten()
}

class ConversionAllUriPattern() : ConversionUriPattern() {
    fun first(init: ConversionFirstUriPattern.() -> Unit) = initMatcher(ConversionFirstUriPattern(), init)
    fun optional(init: ConversionOptionalUriPattern.() -> Unit) = initMatcher(ConversionOptionalUriPattern(), init)
    fun host(hostRegex: String, transform: TransformFunc = null) =
        initMatcher(ConversionHostUriPattern(hostRegex, transform))

    fun path(pathRegex: String, transform: TransformFunc = null) =
        initMatcher(ConversionPathUriPattern(pathRegex, transform))

    fun query(name: String, valueRegex: String, transform: TransformFunc = null) =
        initMatcher(ConversionQueryParamUriPattern(name, valueRegex, transform))

    fun fragment(fragmentRegex: String, transform: TransformFunc = null) =
        initMatcher(ConversionFragmentUriPattern(fragmentRegex, transform))

    override fun matches(uri: Uri): List<ConversionMatcher>? =
        children.mapNotNull { it.matches(uri) }.takeIf { it.size == children.size }?.flatten()
}

class ConversionFirstUriPattern() : ConversionUriPattern() {
    fun all(init: ConversionAllUriPattern.() -> Unit) = initMatcher(ConversionAllUriPattern(), init)

    @Suppress("unused")
    fun optional(init: ConversionOptionalUriPattern.() -> Unit) = initMatcher(ConversionOptionalUriPattern(), init)

    @Suppress("unused")
    fun host(hostRegex: String, transform: TransformFunc = null) =
        initMatcher(ConversionHostUriPattern(hostRegex, transform))

    fun path(pathRegex: String, transform: TransformFunc = null) =
        initMatcher(ConversionPathUriPattern(pathRegex, transform))

    fun query(name: String, valueRegex: String, transform: TransformFunc = null) =
        initMatcher(ConversionQueryParamUriPattern(name, valueRegex, transform))

    fun fragment(fragmentRegex: String, transform: TransformFunc = null) =
        initMatcher(ConversionFragmentUriPattern(fragmentRegex, transform))

    override fun matches(uri: Uri): List<ConversionMatcher>? =
        children.firstNotNullOfOrNull { it.matches(uri) }
}

fun allUriPattern(init: ConversionAllUriPattern.() -> Unit): ConversionAllUriPattern {
    val conversionPattern = ConversionAllUriPattern()
    conversionPattern.init()
    return conversionPattern
}
