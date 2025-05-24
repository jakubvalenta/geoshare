package page.ooooo.geoshare.lib.converters

import page.ooooo.geoshare.lib.GeoUriBuilder
import java.net.URL

abstract class UrlConverter {
    abstract val name: String

    abstract fun isSupportedUrl(url: URL): Boolean

    abstract fun isShortUrl(url: URL): Boolean

    abstract fun parseUrl(url: URL): GeoUriBuilder?

    abstract fun parseHtml(html: String): ParseHtmlResult?

    sealed class ParseHtmlResult {
        data class Parsed(val geoUriBuilder: GeoUriBuilder) : ParseHtmlResult()
        data class Redirect(val url: URL) : ParseHtmlResult()
    }
}
