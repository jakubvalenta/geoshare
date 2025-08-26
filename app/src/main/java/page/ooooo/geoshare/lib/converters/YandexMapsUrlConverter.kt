package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import java.net.URL

class YandexMapsUrlConverter(
    private val log: ILog = DefaultLog(),
    private val uriQuote: UriQuote = DefaultUriQuote(),
) : UrlConverter {
    override val name = "Yandex Maps"

    @StringRes
    override val permissionTitleResId = R.string.converter_yandex_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_yandex_maps_loading_indicator_title

    val anyUrlPattern: Pattern = Pattern.compile("""^https?://yandex.com/maps.+$""")
    val shortUrlPattern: Pattern = Pattern.compile("""^https?://yandex.com/maps/-/.+$""")

    val latRegex = """\+?(?P<lat>-?\d{1,2}(\.\d{1,16})?)"""
    val lonRegex = """\+?(?P<lon>-?\d{1,3}(\.\d{1,16})?)"""
    val coordRegex = "$lonRegex,$latRegex"
    val coordPattern: Pattern = Pattern.compile(coordRegex)
    val zoomRegex = """(?P<z>\d{1,2}(\.\d{1,16})?)"""
    val zoomPattern: Pattern = Pattern.compile(zoomRegex)

    val urlPathPlacePattern: Pattern = Pattern.compile("""^/maps/org/\d+/.*$""")
    val urlQueryPatterns = mapOf(
        "ll" to coordPattern,
    )
    val htmlPattern: Pattern = Pattern.compile("""data-coordinates="$coordRegex"""")

    override fun isSupportedUrl(url: URL): Boolean = anyUrlPattern.matcher(url.toString()).matches()

    override fun isShortUrl(url: URL): Boolean = shortUrlPattern.matcher(url.toString()).matches()

    override fun parseUrl(url: URL): ParseUrlResult? {
        val position = Position()

        // First add coordinates from query param '?ll=<lon>,<lat>'.
        val urlQueryParams = getUrlQueryParams(url.query, uriQuote)
        urlQueryPatterns.map { (paramName, paramPattern) ->
            urlQueryParams[paramName]?.let { paramValue ->
                paramPattern.matcher(paramValue).takeIf { m -> m.matches() }?.let { m ->
                    position.addMatcher(m)
                }
            }
        }

        // THen add zoom from query param '?z=<z>'.
        val zoomMatcher = urlQueryParams["z"]?.let { zoomPattern.matcher(it).takeIf { m -> m.matches() } }
        zoomMatcher?.let { position.addMatcher(it) }

        return if (position.lat != null && position.lon != null) {
            log.i(null, "Yandex Maps URL converted $url > $position")
            ParseUrlResult.Parsed(position)
        } else {
            // If this is a place URL '/maps/org/<id>', then parse coordinates from HTML.
            val urlPath = uriQuote.decode(url.path)
            if (urlPathPlacePattern.matches(urlPath)) {
                log.i(null, "Yandex Maps URL requires HTML parsing to get coordinates for place id $url")
                ParseUrlResult.RequiresHtmlParsing()
            } else {
                log.i(null, "Yandex Maps URL does not contain coordinates or place id $url")
                null
            }
        }
    }

    override fun parseHtml(html: String): ParseHtmlResult? {
        val m = htmlPattern.matcher(html).takeIf { it.find() }
        if (m == null) {
            log.w(null, "Yandex Maps HTML does not contain coordinates")
            return null
        }
        val position = Position()
        position.addMatcher(m)
        log.i(null, "Yandex Maps HTML parsed $position")
        return ParseHtmlResult.Parsed(position)
    }
}
