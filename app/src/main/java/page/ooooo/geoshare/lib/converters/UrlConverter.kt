package page.ooooo.geoshare.lib.converters

import com.google.re2j.Matcher
import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.Position
import java.net.URL

interface UrlConverter {
    val name: String
    val permissionTitleResId: Int
    val loadingIndicatorTitleResId: Int

    fun isSupportedUrl(url: URL): Boolean

    fun isShortUrl(url: URL): Boolean

    fun parseUrl(url: URL): ParseUrlResult?

    fun parseHtml(html: String): ParseHtmlResult?
}

sealed class ParseUrlResult {
    data class Parsed(val position: Position) : ParseUrlResult()
    class RequiresHtmlParsing() : ParseUrlResult()
    data class RequiresHtmlParsingToGetCoords(val position: Position) : ParseUrlResult()
}

sealed class ParseHtmlResult {
    data class Parsed(val position: Position) : ParseHtmlResult()
    data class Redirect(val url: URL) : ParseHtmlResult()
}

abstract class UrlMatcher {
    abstract fun run(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): List<Matcher>?
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

class UrlFirstMatcher(val children: MutableList<UrlMatcher> = mutableListOf()) : UrlMatcher() {
    override fun run(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): List<Matcher>? =
        children.firstNotNullOfOrNull { it.run(urlHost, urlPath, urlQueryParams) }
}

class UrlAllMatcher(val children: MutableList<UrlMatcher> = mutableListOf()) : UrlMatcher() {
    fun first() {}

    override fun run(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): List<Matcher>? =
        children.mapNotNull { it.run(urlHost, urlPath, urlQueryParams) }.flatten().takeIf { it.isNotEmpty() }
}

fun all(init: UrlAllMatcher.() -> Unit): UrlAllMatcher {
    val all = UrlAllMatcher()
    all.init()
    return all
}

fun first(init: UrlFirstMatcher.() -> Unit): UrlFirstMatcher {
    val all = UrlFirstMatcher()
    all.init()
    return all
}
