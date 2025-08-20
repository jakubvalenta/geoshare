package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import page.ooooo.geoshare.lib.Position
import java.net.URL
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
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

    @StringRes
    override val permissionTitleResId = R.string.converter_apple_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_apple_maps_loading_indicator_title

    val fullUrlPattern: Pattern = Pattern.compile("""^https?://maps\.apple\.com/.+$""")
    val shortUrlPattern: Pattern = Pattern.compile("""^https?://maps\.apple/p/.+$""")

    val latRegex = """\+?(?P<lat>-?\d{1,2}(\.\d{1,15})?)"""
    val lonRegex = """\+?(?P<lon>-?\d{1,3}(\.\d{1,15})?)"""
    val coordRegex = "$latRegex,$lonRegex"
    val coordPattern: Pattern = Pattern.compile(coordRegex)
    val zoomRegex = """(?P<z>\d{1,2}(\.\d{1,15})?)"""
    val zoomPattern: Pattern = Pattern.compile(zoomRegex)
    val queryPattern: Pattern = Pattern.compile("""(?P<q>.+)""")

    val urlQueryPatterns = listOf(
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
    val htmlPatterns = listOf(
        Pattern.compile("""<meta property="place:location:latitude" content="$latRegex""""),
        Pattern.compile("""<meta property="place:location:longitude" content="$lonRegex""""),
    )

    override fun isSupportedUrl(url: URL): Boolean =
        fullUrlPattern.matcher(url.toString()).matches() || shortUrlPattern.matcher(url.toString()).matches()

    override fun isShortUrl(url: URL): Boolean =
        false // Treat all URLs as full URLs, because Apple Maps short URLs cannot be unshortened using a HEAD request.

    override fun parseUrl(url: URL): ParseUrlResult? {
        val position = Position()

        val urlQueryParams = getUrlQueryParams(url.query, uriQuote)
        val urlQueryMatchers = urlQueryPatterns.firstNotNullOfOrNull {
            it.map { (paramName, paramPattern) ->
                val paramValue = urlQueryParams[paramName] ?: return@firstNotNullOfOrNull null
                paramPattern.matcher(paramValue).takeIf { m -> m.matches() } ?: return@firstNotNullOfOrNull null
            }
        }
        urlQueryMatchers?.forEach { position.addMatcher(it) }

        val zoomMatcher = urlQueryParams["z"]?.let { zoomPattern.matcher(it).takeIf { m -> m.matches() } }
        zoomMatcher?.let { position.addMatcher(it) }

        return if (position.lat != null && position.lon != null) {
            log.i(null, "Apple Maps URL converted $url > $position")
            ParseUrlResult.Parsed(position)
        } else if (urlQueryParams["auid"] != null || urlQueryParams["place-id"] != null) {
            if (position.q != null) {
                log.i(null, "Apple Maps URL converted but it requires HTML parsing to get coords $url > $position")
                ParseUrlResult.RequiresHtmlParsingToGetCoords(position)
            } else {
                log.i(null, "Apple Maps URL requires HTML parsing to get coordinates for place id $url")
                ParseUrlResult.RequiresHtmlParsing()
            }
        } else if (position.q != null) {
            log.i(null, "Apple Maps URL converted but it contains only query $url > $position")
            ParseUrlResult.Parsed(position)
        } else if (shortUrlPattern.matcher(url.toString()).matches()) {
            ParseUrlResult.RequiresHtmlParsing()
        } else {
            log.i(null, "Apple Maps URL does not contain coordinates or query or place id $url")
            null
        }
    }

    override fun parseHtml(html: String): ParseHtmlResult? {
        val htmlMatchers = htmlPatterns.mapNotNull {
            it.matcher(html).takeIf { m -> m.find() }
        }
        if (htmlMatchers.isEmpty()) {
            log.w(null, "Apple Maps HTML does not match any known pattern")
            return null
        }
        val position = Position()
        htmlMatchers.forEach { position.addMatcher(it) }
        log.i(null, "Apple Maps HTML parsed $position")
        return ParseHtmlResult.Parsed(position)
    }
}
