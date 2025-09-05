package page.ooooo.geoshare.lib.converters

import page.ooooo.geoshare.lib.Position
import java.net.URL

interface UrlConverter {
    val name: String
    val hosts: List<String>
    val shortUrlHosts: List<String>
    val pattern: UrlPattern
    val permissionTitleResId: Int
    val loadingIndicatorTitleResId: Int

    fun parseHtml(html: String): ParseHtmlResult?
}

sealed class ParseHtmlResult {
    data class Parsed(val position: Position) : ParseHtmlResult()
    data class Redirect(val url: URL) : ParseHtmlResult()
}
