package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.Position

abstract class UrlPattern(val supportsHtmlParsing: Boolean = false) {
    val children: MutableList<UrlPattern> = mutableListOf()

    abstract fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): UrlMatch?

    protected fun <T : UrlPattern> initMatcher(urlPattern: T, init: T.() -> Unit = {}): T {
        urlPattern.init()
        children.add(urlPattern)
        return urlPattern
    }
}

class UrlHostPattern(
    val host: Pattern,
    supportsHtmlParsing: Boolean = false,
) : UrlPattern(supportsHtmlParsing = supportsHtmlParsing) {
    override fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): UrlMatch? =
        host.matcher(urlHost)?.takeIf { it.matches() }?.let {
            UrlMatch(Position.fromMatcher(it), supportsHtmlParsing)
        }
}

class UrlPathPattern(
    val path: Pattern,
    supportsHtmlParsing: Boolean = false,
) : UrlPattern(supportsHtmlParsing = supportsHtmlParsing) {
    override fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): UrlMatch? =
        path.matcher(urlPath)?.takeIf { it.matches() }?.let {
            UrlMatch(Position.fromMatcher(it), supportsHtmlParsing)
        }
}

class UrlQueryParamPattern(
    val name: String,
    val value: Pattern,
    supportsHtmlParsing: Boolean = false,
) : UrlPattern(supportsHtmlParsing = supportsHtmlParsing) {
    override fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): UrlMatch? =
        urlQueryParams[name]?.let { value.matcher(it) }?.takeIf { it.matches() }?.let {
            UrlMatch(Position.fromMatcher(it), supportsHtmlParsing)
        }
}

class UrlAllPattern(
    supportsHtmlParsing: Boolean = false,
) : UrlPattern(supportsHtmlParsing = supportsHtmlParsing) {
    fun first(supportsHtmlParsing: Boolean = false, init: UrlFirstPattern.() -> Unit) =
        initMatcher(UrlFirstPattern(supportsHtmlParsing), init)

    fun host(host: Pattern, supportsHtmlParsing: Boolean = false) =
        initMatcher(UrlHostPattern(host, supportsHtmlParsing))

    fun path(path: Pattern, supportsHtmlParsing: Boolean = false) =
        initMatcher(UrlPathPattern(path, supportsHtmlParsing))

    fun queryParam(name: String, value: Pattern, supportsHtmlParsing: Boolean = false) =
        initMatcher(UrlQueryParamPattern(name, value, supportsHtmlParsing))

    override fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): UrlMatch? =
        children.mapNotNull { it.matches(urlHost, urlPath, urlQueryParams) }.takeIf { it.isNotEmpty() }
            ?.reduceRight { sum, element -> sum.union(element) }
            ?.union(UrlMatch(position = null, supportsHtmlParsing = supportsHtmlParsing))
}

class UrlFirstPattern(
    supportsHtmlParsing: Boolean = false,
) : UrlPattern(supportsHtmlParsing = supportsHtmlParsing) {
    fun all(supportsHtmlParsing: Boolean = false, init: UrlAllPattern.() -> Unit) =
        initMatcher(UrlAllPattern(supportsHtmlParsing), init)

    fun host(host: Pattern, supportsHtmlParsing: Boolean = false) =
        initMatcher(UrlHostPattern(host, supportsHtmlParsing))

    fun path(path: Pattern, supportsHtmlParsing: Boolean = false) =
        initMatcher(UrlPathPattern(path, supportsHtmlParsing))

    fun queryParam(name: String, value: Pattern, supportsHtmlParsing: Boolean = false) =
        initMatcher(UrlQueryParamPattern(name, value, supportsHtmlParsing))

    override fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): UrlMatch? =
        children.firstNotNullOfOrNull { it.matches(urlHost, urlPath, urlQueryParams) }
            ?.union(UrlMatch(position = null, supportsHtmlParsing = supportsHtmlParsing))
}

fun all(init: UrlAllPattern.() -> Unit): UrlAllPattern {
    val urlPattern = UrlAllPattern()
    urlPattern.init()
    return urlPattern
}

fun first(init: UrlFirstPattern.() -> Unit): UrlFirstPattern {
    val urlPattern = UrlFirstPattern()
    urlPattern.init()
    return urlPattern
}
