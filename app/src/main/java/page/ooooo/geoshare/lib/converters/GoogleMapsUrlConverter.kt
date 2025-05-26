package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.GeoUriBuilder
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

    val fullUrlPattern: Pattern =
        Pattern.compile("""^https?://((www|maps)\.)?google(\.[a-z]{2,3})?\.[a-z]{2,3}/.+$""")
    val shortUrlPattern: Pattern =
        Pattern.compile("""^https?://(maps\.app\.goo\.gl/|(app\.)?goo\.gl/maps/|g.co/kgs/).+$""")

    val coordRegex =
        """\+?(?P<lat>-?\d{1,2}(\.\d{1,15})?),[+\s]?(?P<lon>-?\d{1,3}(\.\d{1,15})?)"""
    val coordPattern: Pattern = Pattern.compile(coordRegex)
    val dataCoordRegex =
        """!3d(?P<lat>-?\d{1,2}(\.\d{1,15})?)!4d(?P<lon>-?\d{1,3}(\.\d{1,15})?)"""
    val zoomRegex = """(?P<z>\d{1,2}(\.\d{1,15})?)"""
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
        Pattern.compile("""^/maps/dir/.*/$placeRegex$"""),
        Pattern.compile("""^/maps/dir/$"""),
        Pattern.compile("""^/maps/?$"""),
        Pattern.compile("""^/search/?$"""),
        Pattern.compile("""^/?$"""),
    )
    val urlQueryPatterns = listOf<Map<String, Pattern>>(
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
    val googleSearchHtmlPattern: Pattern =
        Pattern.compile("""data-url="(?P<url>[^"]+)""")

    override fun isSupportedUrl(url: URL): Boolean = isFullUrl(url) || isShortUrl(url)

    private fun isFullUrl(url: URL): Boolean =
        fullUrlPattern.matcher(url.toString()).matches()

    override fun isShortUrl(url: URL): Boolean =
        shortUrlPattern.matcher(url.toString()).matches()

    override fun parseUrl(url: URL): GeoUriBuilder? {
        val rawUrlPath = url.path
        if (rawUrlPath == null) {
            log.w(null, "Missing both path and query in Google Maps URL $url")
            return null
        }
        val urlPath = uriQuote.decode(rawUrlPath)
        val urlPathMatcher = urlPathPatterns.firstNotNullOfOrNull {
            it.matcher(urlPath)?.takeIf { it.matches() }
        }
        if (urlPathMatcher == null) {
            log.w(null, "Failed to parse Google Maps URL $url")
            return null
        }

        val geoUriBuilder = GeoUriBuilder(uriQuote)
        geoUriBuilder.fromMatcher(urlPathMatcher)

        val urlQueryParams = getUrlQueryParams(url, uriQuote)
        val urlQueryMatchers = urlQueryPatterns.firstNotNullOfOrNull {
            it.map { (paramName, paramPattern) ->
                val paramValue = urlQueryParams[paramName] ?: return@firstNotNullOfOrNull null
                paramPattern.matcher(paramValue).takeIf { it.matches() } ?: return@firstNotNullOfOrNull null
            }
        }
        urlQueryMatchers?.forEach { geoUriBuilder.fromMatcher(it) }

        val zoomMatcher = urlQueryParams["zoom"]?.let { zoomPattern.matcher(it) }?.takeIf { it.matches() }
        zoomMatcher?.let { geoUriBuilder.fromMatcher(it) }

        log.i(null, "Converted $url to $geoUriBuilder")
        return geoUriBuilder
    }

    override fun parseHtml(html: String): ParseHtmlResult? {
        val geoUriBuilder = parseGoogleMapsHtml(html)
        if (geoUriBuilder !== null) {
            return ParseHtmlResult.Parsed(geoUriBuilder)
        }
        val url = parseGoogleSearchHtml(html)
        if (url !== null) {
            return ParseHtmlResult.Redirect(url)
        }
        return null
    }

    private fun parseGoogleMapsHtml(html: String): GeoUriBuilder? {
        val m = htmlPatterns.firstNotNullOfOrNull {
            val m = it.matcher(html)
            if (m.find()) m else null
        }
        if (m == null) {
            log.w(null, "Failed to parse Google Maps HTML document")
            return null
        }
        val geoUriBuilder = GeoUriBuilder(uriQuote = uriQuote)
        geoUriBuilder.fromMatcher(m)
        log.i(null, "Parsed HTML document to $geoUriBuilder")
        return geoUriBuilder
    }

    private fun parseGoogleSearchHtml(html: String): URL? {
        val m = googleSearchHtmlPattern.matcher(html)
            .let { if (it.find()) it else null }
        if (m == null) {
            log.w(null, "Failed to parse Google Search HTML document")
            return null
        }
        val relativeOrAbsoluteUrlString = m.group("url")
        val absoluteUrlString = relativeOrAbsoluteUrlString.replace(
            "^/".toRegex(),
            "https://www.google.com/"
        )
        val absoluteUrl = try {
            URL(absoluteUrlString)
        } catch (_: MalformedURLException) {
            log.w(null, "Invalid URL $absoluteUrlString")
            return null
        }
        log.i(null, "Parsed Google Search HTML document to $absoluteUrl")
        return absoluteUrl
    }
}
