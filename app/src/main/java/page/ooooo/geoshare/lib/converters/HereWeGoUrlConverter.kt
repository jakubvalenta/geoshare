package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import java.net.URL
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class HereWeGoUrlConverter(
    private val log: ILog = DefaultLog(),
    private val uriQuote: UriQuote = DefaultUriQuote(),
) : UrlConverter {
    override val name: String = "HERE WeGo"

    @StringRes
    override val permissionTitleResId = R.string.converter_here_we_go_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_here_we_go_loading_indicator_title

    val fullUrlPattern: Pattern = Pattern.compile("""^https?://wego\.here.com/.+$""")
    val shortUrlPattern: Pattern = Pattern.compile("""^https?://share\.here.com/p/.-.+$""")

    val latRegex = """\+?(?P<lat>-?\d{1,2}(\.\d{1,16})?)"""
    val lonRegex = """\+?(?P<lon>-?\d{1,3}(\.\d{1,16})?)"""
    val zoomRegex = """(?P<z>\d{1,2}(\.\d{1,16})?)"""
    val simpleBase64Regex = """[A-Za-z0-9+/]+=*"""

    val urlPathPattern: Pattern = Pattern.compile("""^/p/[a-z]-(?P<coordString>$simpleBase64Regex)$""")
    val urlQueryPatterns = mapOf(
        "map" to Pattern.compile("""$latRegex,$lonRegex,$zoomRegex"""),
    )
    val coordStringPatterns = listOf(
        Pattern.compile("""(lat=|"latitude":)$latRegex"""),
        Pattern.compile("""(lon=|"longitude":)$lonRegex"""),
    )

    override fun isSupportedUrl(url: URL): Boolean =
        fullUrlPattern.matcher(url.toString()).matches() || shortUrlPattern.matcher(url.toString()).matches()

    override fun isShortUrl(url: URL): Boolean =
        false // Treat all URLs as full URLs, because HERE WeGo URLs don't need unshortening to extract coordinates.

    @OptIn(ExperimentalEncodingApi::class)
    override fun parseUrl(url: URL): ParseUrlResult? {
        val position = Position()

        // First add position information from query params, e.g. '?map=<lat>,<lon>,<z>'
        val urlQueryParams = getUrlQueryParams(url.query, uriQuote)
        urlQueryPatterns.map { (paramName, paramPattern) ->
            urlQueryParams[paramName]?.let { paramValue ->
                paramPattern.matcher(paramValue).takeIf { m -> m.matches() }?.let { m ->
                    position.addMatcher(m)
                }
            }
        }

        // Then add position information from path, e.g. '/s-<base64>', possibly overwriting the info from query params.
        val urlPath = uriQuote.decode(url.path)
        urlPathPattern.matcher(urlPath).takeIf { m -> m.matches() }?.group("coordString")
            ?.let { coordStringEncoded -> Base64.decode(coordStringEncoded).decodeToString() }?.let { coordString ->
                for (coordStringPattern in coordStringPatterns) {
                    coordStringPattern.matcher(coordString).takeIf { m -> m.find() }
                        ?.let { m -> position.addMatcher(m) }
                }
            }

        return if (position.lat != null && position.lon != null) {
            log.i(null, "Here WeGo URL converted $url > $position")
            ParseUrlResult.Parsed(position)
        } else {
            log.i(null, "Here WeGo URL does not contain coordinates $url")
            null
        }
    }

    override fun parseHtml(html: String) = null
}
