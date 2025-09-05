package page.ooooo.geoshare.lib.converters

import com.google.re2j.Matcher
import com.google.re2j.Pattern

abstract class UrlMatcher {
    val children: MutableList<UrlMatcher> = mutableListOf()

    abstract fun run(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): List<Matcher>?

    protected fun <T : UrlMatcher> initMatcher(matcher: T, init: T.() -> Unit = {}): T {
        matcher.init()
        children.add(matcher)
        return matcher
    }
}

class UrlHostMatcher(val host: Pattern) : UrlMatcher() {
    override fun run(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): List<Matcher>? =
        host.matcher(urlHost)?.takeIf { it.matches() }?.let { listOf(it) }
}

class UrlPathMatcher(val path: Pattern) : UrlMatcher() {
    override fun run(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): List<Matcher>? =
        path.matcher(urlPath)?.takeIf { it.matches() }?.let { listOf(it) }
}

class UrlQueryParamsMatcher(val queryParams: Map<String, Pattern>) : UrlMatcher() {
    override fun run(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): List<Matcher>? =
        queryParams.map { (paramName, paramPattern) ->
            urlQueryParams[paramName]?.let { paramPattern.matcher(it) }?.takeIf { it.matches() } ?: return@run null
        }
}

class UrlFirstMatcher() : UrlMatcher() {
    fun all(init: UrlAllMatcher.() -> Unit) = initMatcher(UrlAllMatcher(), init)
    fun host(host: Pattern) = initMatcher(UrlHostMatcher(host))
    fun path(path: Pattern) = initMatcher(UrlPathMatcher(path))
    fun queryParams(queryParams: Map<String, Pattern>) = initMatcher(UrlQueryParamsMatcher(queryParams))

    override fun run(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): List<Matcher>? =
        children.firstNotNullOfOrNull { it.run(urlHost, urlPath, urlQueryParams) }
}

class UrlAllMatcher() : UrlMatcher() {
    fun first(init: UrlFirstMatcher.() -> Unit) = initMatcher(UrlFirstMatcher(), init)
    fun host(host: Pattern) = initMatcher(UrlHostMatcher(host))
    fun path(path: Pattern) = initMatcher(UrlPathMatcher(path))
    fun queryParams(queryParams: Map<String, Pattern>) = initMatcher(UrlQueryParamsMatcher(queryParams))

    override fun run(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): List<Matcher>? =
        children.mapNotNull { it.run(urlHost, urlPath, urlQueryParams) }.flatten().takeIf { it.isNotEmpty() }
}

fun all(init: UrlAllMatcher.() -> Unit): UrlAllMatcher {
    val matcher = UrlAllMatcher()
    matcher.init()
    return matcher
}

fun first(init: UrlFirstMatcher.() -> Unit): UrlFirstMatcher {
    val matcher = UrlFirstMatcher()
    matcher.init()
    return matcher
}
