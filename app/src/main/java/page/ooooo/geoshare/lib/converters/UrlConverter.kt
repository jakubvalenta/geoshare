package page.ooooo.geoshare.lib.converters

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
