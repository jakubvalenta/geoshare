package page.ooooo.geoshare.lib.converters

import page.ooooo.geoshare.lib.GeoUriBuilder
import java.net.URL
import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.getUrlQueryParams

class AppleMapsUrlConverter(
    private val log: ILog = DefaultLog(),
    private val uriQuote: UriQuote = DefaultUriQuote(),
) : UrlConverter {
    override val name = "Apple Maps"

    val urlPattern: Pattern = Pattern.compile("""^https?://maps\.apple\.com/.+$""")

    val coordRegex =
        """\+?(?P<lat>-?\d{1,2}(\.\d{1,15})?),[+\s]?(?P<lon>-?\d{1,3}(\.\d{1,15})?)"""
    val coordPattern: Pattern = Pattern.compile(coordRegex)
    val zoomRegex = """(?P<z>\d{1,2}(\.\d{1,15})?)"""
    val zoomPattern: Pattern = Pattern.compile(zoomRegex)
    val queryPattern: Pattern = Pattern.compile("""(?P<q>.+)""")

    val urlQueryPatterns = listOf<Map<String, Pattern>>(
        mapOf("ll" to coordPattern),
        mapOf("coordinate" to coordPattern),
        mapOf("q" to coordPattern),
        mapOf("address" to queryPattern),
        mapOf("name" to queryPattern),
        mapOf("q" to queryPattern, "sll" to coordPattern),
        mapOf("q" to queryPattern),
        mapOf("sll" to coordPattern),
        mapOf("center" to coordPattern),
    )

    override fun isSupportedUrl(url: URL): Boolean = urlPattern.matcher(url.toString()).matches()

    override fun isShortUrl(url: URL): Boolean = false

    override fun parseUrl(url: URL): GeoUriBuilder? {
        val geoUriBuilder = GeoUriBuilder(uriQuote)

        val urlQueryParams = getUrlQueryParams(url, uriQuote)
        val urlQueryMatchers = urlQueryPatterns.firstNotNullOfOrNull {
            it.map { (paramName, paramPattern) ->
                val paramValue = urlQueryParams[paramName] ?: return@firstNotNullOfOrNull null
                paramPattern.matcher(paramValue).takeIf { it.matches() } ?: return@firstNotNullOfOrNull null
            }
        }
        if (urlQueryMatchers == null) {
            log.w(null, "Failed to parse Google Maps URL $url")
            return null
        }
        urlQueryMatchers.forEach { geoUriBuilder.fromMatcher(it) }

        val zoomMatcher = urlQueryParams["z"]?.let { zoomPattern.matcher(it) }?.takeIf { it.matches() }
        zoomMatcher?.let { geoUriBuilder.fromMatcher(it) }

        log.i(null, "Converted $url to $geoUriBuilder")
        return geoUriBuilder
    }

    override fun parseHtml(html: String): ParseHtmlResult? = null
}
