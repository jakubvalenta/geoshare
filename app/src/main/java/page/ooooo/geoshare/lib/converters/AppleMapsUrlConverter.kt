package page.ooooo.geoshare.lib.converters

import android.R.attr.queryPattern
import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import java.net.URL

class AppleMapsUrlConverter(
    private val log: ILog = DefaultLog(),
    private val uriQuote: UriQuote = DefaultUriQuote(),
) : UrlConverter {
    override val name = "Apple Maps"
    override val hosts = listOf("maps.apple.com", "maps.apple")
    override val shortUrlHosts = emptyList<String>()
    override val pattern = all {
        queryParam("z", zoomPattern)
        first {
            all {
                host(Pattern.compile("maps.apple"))
                path(Pattern.compile("/p/.+"))
            }
            queryParam("ll", coordPattern)
            queryParam("coordinate", coordPattern)
            queryParam("q", coordPattern)
            queryParam("address", queryPattern)
            queryParam("name", queryPattern)
            all(supportsHtmlParsing = true) {
                queryParam("auid", ".")
                queryParam("q", "<q>")
            }
            all(supportsHtmlParsing = true) {
                queryParam("place-id", ".")
                queryParam("q", "<q>")
            }
            queryParam("auid", ".", supportsHtmlParsing = true)
            queryParam("place-id", ".", supportsHtmlParsing = true)
            all {
                queryParam("q", queryPattern)
                queryParam("sll", coordPattern)
            }
            queryParam("q", queryPattern)
            queryParam("sll", coordPattern)
            queryParam("center", coordPattern)
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_apple_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_apple_maps_loading_indicator_title

    val latRegex = """\+?(?P<lat>-?\d{1,2}(\.\d{1,16})?)"""
    val lonRegex = """\+?(?P<lon>-?\d{1,3}(\.\d{1,16})?)"""
    val coordRegex = "$latRegex,$lonRegex"
    val coordPattern: Pattern = Pattern.compile(coordRegex)
    val zoomRegex = """(?P<z>\d{1,2}(\.\d{1,16})?)"""
    val zoomPattern: Pattern = Pattern.compile(zoomRegex)
    val queryPattern: Pattern = Pattern.compile("""(?P<q>.+)""")

    val htmlPatterns = listOf(
        Pattern.compile("""<meta property="place:location:latitude" content="$latRegex""""),
        Pattern.compile("""<meta property="place:location:longitude" content="$lonRegex""""),
    )

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
