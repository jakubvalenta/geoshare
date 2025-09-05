package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.Position
import java.net.URL

interface UrlConverter {
    val name: String
    val host: Pattern
    val shortUrlHost: Pattern?
    val pattern: UrlPattern
    val permissionTitleResId: Int
    val loadingIndicatorTitleResId: Int
    val parseHtml: ((html: String) -> ParseHtmlResult?)?
}

sealed class ParseHtmlResult {
    data class Parsed(val position: Position) : ParseHtmlResult()
    data class Redirect(val url: URL) : ParseHtmlResult()
}
