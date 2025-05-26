package page.ooooo.geoshare.lib.converters

import page.ooooo.geoshare.lib.GeoUriBuilder
import java.net.URL

interface UrlConverter {
    val name: String

    fun isSupportedUrl(url: URL): Boolean

    fun isShortUrl(url: URL): Boolean

    fun parseUrl(url: URL): GeoUriBuilder?

    fun parseHtml(html: String): ParseHtmlResult?
}

sealed class ParseHtmlResult {
    data class Parsed(val geoUriBuilder: GeoUriBuilder) : ParseHtmlResult()
    data class Redirect(val url: URL) : ParseHtmlResult()
}
