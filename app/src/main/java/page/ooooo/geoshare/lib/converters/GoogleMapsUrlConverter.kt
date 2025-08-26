package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.getUrlQueryParams
import java.net.MalformedURLException
import java.net.URL

class GoogleMapsUrlConverter(
    private val log: ILog = DefaultLog(),
    private val uriQuote: UriQuote = DefaultUriQuote(),
) : UrlConverter {
    override val name = "Google Maps"

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title

    val fullUrlPattern: Pattern = Pattern.compile("""^https?://((www|maps)\.)?google(\.[a-z]{2,3})?\.[a-z]{2,3}/.+$""")
    val shortUrlPattern: Pattern =
        Pattern.compile("""^https?://(maps\.app\.goo\.gl/|(app\.)?goo\.gl/maps/|g.co/kgs/).+$""")

    val coordRegex = """\+?(?P<lat>-?\d{1,2}(\.\d{1,16})?),[+\s]?(?P<lon>-?\d{1,3}(\.\d{1,16})?)"""
    val coordPattern: Pattern = Pattern.compile(coordRegex)
    val dataCoordRegex = """!3d(?P<lat>-?\d{1,2}(\.\d{1,16})?)!4d(?P<lon>-?\d{1,3}(\.\d{1,16})?)"""
    val zoomRegex = """(?P<z>\d{1,2}(\.\d{1,16})?)"""
    val zoomPattern: Pattern = Pattern.compile(zoomRegex)
    val queryPattern: Pattern = Pattern.compile("""(?P<q>.+)""")
    val placeRegex = """(?P<q>[^/]+)"""

    @Suppress("SpellCheckingInspection")
    val urlPathPatterns = listOf(
        Pattern.compile("""^/maps/.*/@[\d.,+-]+,${zoomRegex}z/data=.*$dataCoordRegex.*$"""),
        Pattern.compile("""^/maps/.*/data=.*$dataCoordRegex.*$"""),
        Pattern.compile("""^/maps/@$coordRegex,${zoomRegex}z.*$"""),
        Pattern.compile("""^/maps/@$coordRegex.*$"""),
        Pattern.compile("""^/maps/@$"""),
        Pattern.compile("""^/maps/place/$coordRegex/@[\d.,+-]+,${zoomRegex}z.*$"""),
        Pattern.compile("""^/maps/place/$placeRegex/@$coordRegex,${zoomRegex}z.*$"""),
        Pattern.compile("""^/maps/place/$placeRegex/@$coordRegex.*$"""),
        Pattern.compile("""^/maps/place/$coordRegex.*$"""),
        Pattern.compile("""^/maps/place/$placeRegex.*$"""),
        Pattern.compile("""^/maps/place//.*$"""),
        Pattern.compile("""^/maps/placelists/list/.*$"""),
        Pattern.compile("""^/maps/search/$coordRegex.*$"""),
        Pattern.compile("""^/maps/search/$placeRegex.*$"""),
        Pattern.compile("""^/maps/search/$"""),
        Pattern.compile("""^/maps/dir/.*/$coordRegex/data[^/]*$"""),
        Pattern.compile("""^/maps/dir/.*/$placeRegex/data[^/]*$"""),
        Pattern.compile("""^/maps/dir/.*/$coordRegex$"""),
        Pattern.compile("""^/maps/dir/.*/@$coordRegex,${zoomRegex}z.*$"""),
        Pattern.compile("""^/maps/dir/.*/$placeRegex$"""),
        Pattern.compile("""^/maps/dir/$"""),
        Pattern.compile("""^/maps/?$"""),
        Pattern.compile("""^/search/?$"""),
        Pattern.compile("""^/?$"""),
    )
    val urlQueryPatterns = listOf(
        mapOf("destination" to coordPattern),
        mapOf("destination" to queryPattern),
        mapOf("q" to coordPattern),
        mapOf("q" to queryPattern),
        mapOf("query" to coordPattern),
        mapOf("query" to queryPattern),
        mapOf("viewpoint" to coordPattern),
        mapOf("center" to coordPattern),
    )
    val htmlPatterns = listOf(
        Pattern.compile("""/@$coordRegex"""),
        Pattern.compile("""\[null,null,$coordRegex\]"""),
    )
    val googleSearchHtmlPattern: Pattern = Pattern.compile("""data-url="(?P<url>[^"]+)""")

    override fun isSupportedUrl(url: URL): Boolean = isFullUrl(url) || isShortUrl(url)

    private fun isFullUrl(url: URL): Boolean = fullUrlPattern.matcher(url.toString()).matches()

    override fun isShortUrl(url: URL): Boolean = shortUrlPattern.matcher(url.toString()).matches()

    override fun parseUrl(url: URL): ParseUrlResult? {
        val urlPath = uriQuote.decode(url.path)
        val urlPathMatcher = urlPathPatterns.firstNotNullOfOrNull {
            it.matcher(urlPath).takeIf { m -> m.matches() }
        }
        if (urlPathMatcher == null) {
            log.w(null, "Google Maps URL does not match any known path pattern $url")
            return null
        }

        val position = Position()
        position.addMatcher(urlPathMatcher)

        val urlQueryParams = getUrlQueryParams(url.query, uriQuote)
        val urlQueryMatchers = urlQueryPatterns.firstNotNullOfOrNull {
            it.map { (paramName, paramPattern) ->
                val paramValue = urlQueryParams[paramName] ?: return@firstNotNullOfOrNull null
                paramPattern.matcher(paramValue).takeIf { m -> m.matches() } ?: return@firstNotNullOfOrNull null
            }
        }
        urlQueryMatchers?.forEach { position.addMatcher(it) }

        val zoomMatcher = urlQueryParams["zoom"]?.let { zoomPattern.matcher(it).takeIf { m -> m.matches() } }
        zoomMatcher?.let { position.addMatcher(it) }

        return if (position.lat != null && position.lon != null) {
            log.i(null, "Google Maps URL converted $url > $position")
            ParseUrlResult.Parsed(position)
        } else {
            log.i(null, "Google Maps URL converted but it requires HTML parsing to get coords $url > $position")
            ParseUrlResult.RequiresHtmlParsingToGetCoords(position)
        }
    }

    override fun parseHtml(html: String): ParseHtmlResult? =
        parseGoogleMapsHtml(html)?.let { ParseHtmlResult.Parsed(it) }
            ?: parseGoogleSearchHtml(html)?.let { ParseHtmlResult.Redirect(it) }

    private fun parseGoogleMapsHtml(html: String): Position? {
        val m = htmlPatterns.firstNotNullOfOrNull {
            it.matcher(html).takeIf { m -> m.find() }
        }
        if (m == null) {
            log.w(null, "Google Maps HTML does not match any known pattern")
            return null
        }
        val position = Position()
        position.addMatcher(m)
        log.i(null, "Google Maps HTML parsed $position")
        return position
    }

    private fun parseGoogleSearchHtml(html: String): URL? {
        val m = googleSearchHtmlPattern.matcher(html).takeIf { it.find() }
        if (m == null) {
            log.w(null, "Google Search HTML does not contain a Google Maps URL")
            return null
        }
        val relativeOrAbsoluteUrlString = m.group("url")
        val absoluteUrlString = relativeOrAbsoluteUrlString.replace(
            "^/".toRegex(), "https://www.google.com/"
        )
        val absoluteUrl = try {
            URL(absoluteUrlString)
        } catch (_: MalformedURLException) {
            log.i(null, "Google Search HTML contains an invalid Google Maps URL $absoluteUrlString")
            return null
        }
        log.i(null, "Google Search HTML parsed $absoluteUrl")
        return absoluteUrl
    }
}
