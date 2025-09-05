package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.Position

abstract class UrlPattern() {
    val children: MutableList<UrlPattern> = mutableListOf()

    abstract fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): Position?

    protected fun <T : UrlPattern> initMatcher(urlPattern: T, init: T.() -> Unit = {}): T {
        urlPattern.init()
        children.add(urlPattern)
        return urlPattern
    }
}

class UrlHostPattern(
    val host: Pattern,
) : UrlPattern() {
    override fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): Position? =
        host.matcher(urlHost)?.takeIf { it.matches() }?.let { Position.fromMatcher(it) }
}

class UrlPathPattern(
    val path: Pattern,
) : UrlPattern() {
    override fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): Position? =
        path.matcher(urlPath)?.takeIf { it.matches() }?.let { Position.fromMatcher(it) }
}

class UrlQueryParamPattern(
    val name: String,
    val value: Pattern,
) : UrlPattern() {
    override fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): Position? =
        urlQueryParams[name]?.let { value.matcher(it) }?.takeIf { it.matches() }?.let { Position.fromMatcher(it) }
}

class UrlAllPattern() : UrlPattern() {
    fun first(init: UrlFirstPattern.() -> Unit) = initMatcher(UrlFirstPattern(), init)
    fun host(host: Pattern) = initMatcher(UrlHostPattern(host))
    fun path(path: Pattern) = initMatcher(UrlPathPattern(path))
    fun query(name: String, value: Pattern) = initMatcher(UrlQueryParamPattern(name, value))

    override fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): Position? =
        children.mapNotNull { it.matches(urlHost, urlPath, urlQueryParams) }.takeIf { it.isNotEmpty() }
            ?.reduceRight { sum, element -> sum.union(element) }
}

class UrlFirstPattern() : UrlPattern() {
    fun all(init: UrlAllPattern.() -> Unit) = initMatcher(UrlAllPattern(), init)
    fun path(path: Pattern) = initMatcher(UrlPathPattern(path))
    fun query(name: String, value: Pattern) = initMatcher(UrlQueryParamPattern(name, value))

    override fun matches(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): Position? =
        children.firstNotNullOfOrNull { it.matches(urlHost, urlPath, urlQueryParams) }
}

fun all(init: UrlAllPattern.() -> Unit): UrlAllPattern {
    val urlPattern = UrlAllPattern()
    urlPattern.init()
    return urlPattern
}
