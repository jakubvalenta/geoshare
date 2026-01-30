package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.collections.immutable.ImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.findAllNaivePoint
import page.ooooo.geoshare.lib.extensions.findNaivePoint
import page.ooooo.geoshare.lib.extensions.findUriString
import page.ooooo.geoshare.lib.extensions.forEachReversed
import page.ooooo.geoshare.lib.extensions.matchNaivePoint
import page.ooooo.geoshare.lib.extensions.matchQ
import page.ooooo.geoshare.lib.extensions.matchZ
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.asGCJ02
import page.ooooo.geoshare.lib.point.buildPoints
import page.ooooo.geoshare.lib.point.toParseHtmlResult
import page.ooooo.geoshare.lib.point.toParseUriResult

object GoogleMapsInput : Input.HasShortUri, Input.HasHtml {
    private const val SHORT_URL = """((maps\.)?(app\.)?goo\.gl|g\.co)/[/A-Za-z0-9_-]+"""

    override val uriPattern: Pattern =
        Pattern.compile("""(https?://)?((www|maps)\.)?(google(\.[a-z]{2,3})?\.[a-z]{2,3}[/?#]\S+|$SHORT_URL)""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.GOOGLE_MAPS,
        nameResId = R.string.converter_google_maps_name,
        items = listOf(
            InputDocumentationItem.Url(5, "https://maps.app.goo.gl"),
            InputDocumentationItem.Url(5, "https://app.goo.gl/maps"),
            InputDocumentationItem.Url(5, "https://maps.google.com"),
            InputDocumentationItem.Url(5, "https://goo.gl/maps"),
            InputDocumentationItem.Url(5, "https://google.com/maps"),
            InputDocumentationItem.Url(5, "https://www.google.com/maps"),
            InputDocumentationItem.Url(10, "https://g.co/kgs"),
        ),
    )
    override val shortUriPattern: Pattern = Pattern.compile("""(https?://)?$SHORT_URL""")
    override val shortUriMethod = Input.ShortUriMethod.HEAD

    override suspend fun parseUri(uri: Uri): ParseUriResult? {
        var htmlUriString: String? = null
        return buildPoints {
            uri.run {
                // Try query parameters for all URLs
                setPointIfNull { LAT_LON_PATTERN matchNaivePoint queryParams["destination"] }
                setPointIfNull { LAT_LON_PATTERN matchNaivePoint queryParams["q"] }
                setPointIfNull { LAT_LON_PATTERN matchNaivePoint queryParams["query"] }
                setPointIfNull { LAT_LON_PATTERN matchNaivePoint queryParams["ll"] }
                setPointIfNull { LAT_LON_PATTERN matchNaivePoint queryParams["viewpoint"] }
                setPointIfNull { LAT_LON_PATTERN matchNaivePoint queryParams["center"] }
                setNameIfNull { Q_PARAM_PATTERN matchQ queryParams["destination"] }
                setNameIfNull { Q_PARAM_PATTERN matchQ queryParams["q"] }
                setNameIfNull { Q_PARAM_PATTERN matchQ queryParams["query"] }
                setZIfNull { Z_PATTERN matchZ queryParams["zoom"] }

                val parseUriParts = setOf("dir", "place", "search")
                val parseHtmlParts = setOf("", "@", "d", "placelists")
                val parseHtmlExcludeParts = setOf("search")
                val parts = uri.pathParts.drop(1).dropWhile { it == "maps" }
                val firstPart = parts.firstOrNull()
                when {
                    firstPart == null || firstPart in parseHtmlParts -> {
                        // Skip URI parsing and go to HTML parsing
                        htmlUriString = uri.toString()
                    }

                    firstPart in parseUriParts || firstPart.startsWith('@') -> {
                        // Parse URI
                        var defaultCoords: Pair<Double, Double>? = null
                        val pointPattern: Pattern = Pattern.compile("""$LAT,$LON.*""")
                        parts.dropWhile { it in parseUriParts }.forEachReversed { part ->
                            if (part.startsWith("data=")) {
                                setPointIfNull { """!3d$LAT!4d$LON""" findNaivePoint part }
                                points.addAll("""!1d$LON!2d$LAT""" findAllNaivePoint part)
                            } else if (part.startsWith('@') && defaultCoords == null) {
                                ("""@$LAT,$LON(,${Z}z)?.*""" matchNaivePoint part)?.let { (lat, lon, z) ->
                                    lat?.let { lat ->
                                        lon?.let { lon ->
                                            defaultCoords = lat to lon
                                            defaultZ = z
                                        }
                                    }
                                }
                            } else {
                                setPointIfNull { pointPattern matchNaivePoint part }
                                setNameIfNull { Q_PATH_PATTERN matchQ part }
                            }
                        }
                        if (points.isEmpty()) {
                            if (defaultCoords != null) {
                                points.add(NaivePoint(defaultCoords.first, defaultCoords.second))
                            } else if (firstPart !in parseHtmlExcludeParts) {
                                // Go to HTML parsing if needed
                                htmlUriString = uri.toString()
                            }
                        }
                    }
                }
            }
        }
            .asGCJ02()
            .toParseUriResult(htmlUriString)
    }

    override suspend fun parseHtml(
        channel: ByteReadChannel,
        pointsFromUri: ImmutableList<Point>,
        log: ILog,
    ): ParseHtmlResult? {
        var redirectUriString: String? = null
        return buildPoints {
            defaultName = pointsFromUri.lastOrNull()?.name

            var defaultPoint: NaivePoint? = null
            var genericMetaTagFound = false
            val pointPattern = Pattern.compile("""\[(null,null,|null,\[)$LAT,$LON\]""")
            val defaultPointLinkPattern = Pattern.compile("""/@$LAT,$LON""")
            val defaultPointAppInitStatePattern =
                Pattern.compile("""APP_INITIALIZATION_STATE=\[\[\[[\d.-]+,$LON,$LAT""")
            val genericMetaTagPattern = Pattern.compile(
                @Suppress("SpellCheckingInspection") """<meta content="Google Maps" itemprop="name""""
            )
            val uriPattern = Pattern.compile("""data-url="(?P<url>[^"]+)"""")

            while (true) {
                val line = channel.readUTF8Line() ?: break
                if (!genericMetaTagFound && (genericMetaTagPattern find line) != null) {
                    log.d("GoogleMapsInput", "HTML Pattern: Generic meta tag matched line $line")
                    genericMetaTagFound = true
                }
                if (addPoints { (pointPattern findAllNaivePoint line) }) {
                    log.d("GoogleMapsInput", "HTML Pattern: Point pattern matched line $line")
                }
                if (defaultPoint == null) {
                    (defaultPointLinkPattern findNaivePoint line)?.let {
                        log.d("GoogleMapsInput", "HTML Pattern: Default point pattern 1 matched line $line")
                        defaultPoint = it
                    }
                }
                if (defaultPoint == null && !genericMetaTagFound) {
                    // When the HTML contains a generic "Google Maps" META tag instead of a specific one like
                    // "Berlin - Germany", then it seems that the APP_INITIALIZATION_STATE contains coordinates of the
                    // IP address that the HTTP request came from, instead of correct coordinates. So let's ignore the
                    // coordinates.
                    (defaultPointAppInitStatePattern findNaivePoint line)?.let {
                        log.d("GoogleMapsInput", "HTML Pattern: Default point pattern 2 matched line $line")
                        defaultPoint = it
                    }
                }
                if (redirectUriString == null) {
                    (uriPattern findUriString line)?.let {
                        log.d("GoogleMapsInput", "HTML Pattern: URI pattern matched line $line")
                        redirectUriString = it
                    }
                }
            }

            if (points.isEmpty() && defaultPoint != null) {
                points.add(defaultPoint)
            }
        }
            .asGCJ02()
            .toParseHtmlResult(redirectUriString)
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
}
