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
    val queryPatterns = mapOf<String, List<Pattern>>(
        // Later query patterns overwrite earlier ones.
        "center" to listOf(coordPattern),
        "sll" to listOf(coordPattern),
        "coordinate" to listOf(coordPattern),
        "ll" to listOf(coordPattern),
        "q" to listOf(coordPattern, queryPattern),
        "name" to listOf(queryPattern),
        "address" to listOf(queryPattern),
        "z" to listOf(zoomPattern),
    )

    override fun isSupportedUrl(url: URL): Boolean = urlPattern.matcher(url.toString()).matches()

    override fun isShortUrl(url: URL): Boolean = false

    override fun parseUrl(url: URL): GeoUriBuilder? {
        val geoUriBuilder = GeoUriBuilder(uriQuote = uriQuote)
        val urlQueryParams = getUrlQueryParams(url, uriQuote)
        for (queryPattern in queryPatterns) {
            val paramName = queryPattern.key
            val paramValue = urlQueryParams[paramName] ?: continue
            val patterns = queryPattern.value
            val m = patterns.firstNotNullOfOrNull {
                val m = it.matcher(paramValue)
                if (m.matches()) m else null
            } ?: continue
            geoUriBuilder.fromMatcher(m)
        }
        log.i(null, "Converted $url to $geoUriBuilder")
        return geoUriBuilder
    }

    override fun parseHtml(html: String): ParseHtmlResult? = null
}
