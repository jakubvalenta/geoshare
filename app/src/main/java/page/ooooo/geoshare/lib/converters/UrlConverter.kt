package page.ooooo.geoshare.lib.converters

import android.R.attr.host
import android.R.attr.path
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.Position
import java.net.URL
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

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
    abstract fun run(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): Position
}

class UrlHostMatcher(val host: Pattern) : UrlMatcher() {
    override fun run(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): Position? =
        host.matcher(urlHost)?.takeIf { it.matches() }?.let { Position().apply { addMatcher(it) } }

}

class UrlPathMatcher(val path: Pattern) : UrlMatcher() {
    override fun run(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): Position? =
        path.matcher(urlPath)?.takeIf { it.matches() }?.let { Position().apply { addMatcher(it) } }
}

class UrlQueryParamsMatcher(val queryParams: Map<String, Pattern>) : UrlMatcher() {
    override fun run(urlHost: String, urlPath: String, urlQueryParams: Map<String, String>): Position? =
        Position().apply {
            for ((paramName, paramPattern) in queryParams) {
                urlQueryParams[paramName]?.let { paramPattern.matcher(it) }?.takeIf { it.matches() }
                    ?.let { addMatcher(it) } ?: return null
            }
        }
}

class All {
    val children = arrayListOf<UrlMatcher>()
    fun evaluate(): Position? {
        TODO("Not implemented yet")
    }
}

fun all(init: All.() -> Unit): All {
    val all = All()
    all.init()
    return all
}
