package page.ooooo.geoshare.lib.converters

import page.ooooo.geoshare.lib.GeoUriBuilder
import java.net.URL

interface UrlConverter {
    val name: String

    fun isSupportedUrl(url: URL): Boolean

    fun isShortUrl(url: URL): Boolean

    fun parseUrl(url: URL): ParseUrlResult?

    fun parseHtml(html: String): ParseHtmlResult?
}

sealed class ParseUrlResult {
    data class Parsed(val geoUriBuilder: GeoUriBuilder) : ParseUrlResult()
    class RequiresHtmlParsing() : ParseUrlResult()
    data class RequiresHtmlParsingToGetCoords(val geoUriBuilder: GeoUriBuilder) : ParseUrlResult()
}

sealed class ParseHtmlResult {
    data class Parsed(val geoUriBuilder: GeoUriBuilder) : ParseHtmlResult()
    data class Redirect(val url: URL) : ParseHtmlResult()
}
