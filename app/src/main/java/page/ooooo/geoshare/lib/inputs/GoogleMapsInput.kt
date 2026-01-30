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
import page.ooooo.geoshare.lib.extensions.findAllPoints
import page.ooooo.geoshare.lib.extensions.findPoint
import page.ooooo.geoshare.lib.extensions.forEachReversed
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchName
import page.ooooo.geoshare.lib.extensions.matchPoint
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

                listOf("destination", "q", "query", "ll", "viewpoint", "center")
                    .firstNotNullOfOrNull { key -> LAT_LON_PATTERN matchPoint queryParams[key] }
                    ?.also { points.add(it) }

                listOf("destination", "q", "query")
                    .firstNotNullOfOrNull { key -> Q_PARAM_PATTERN matchName queryParams[key] }
                    ?.also { defaultName = it }

                (Z_PATTERN matchZ queryParams["zoom"])?.also { defaultZ = it }

                // Parse path parts

                val parseUriParts = setOf("dir", "place", "search")
                val parseHtmlParts = setOf("", "@", "d", "placelists")
                val parseHtmlExcludeParts = setOf("search")
                val parts = uri.pathParts.drop(1).dropWhile { it == "maps" }
                val firstPart = parts.firstOrNull()
                if (firstPart == null || firstPart in parseHtmlParts) {
                    // Skip URI parsing and go to HTML parsing
                    htmlUriString = uri.toString()
                } else if (firstPart in parseUriParts || firstPart.startsWith('@')) {
                    // Iterate path parts in reverse order
                    var centerCoords: Pair<Double, Double>? = null
                    val pointPattern: Pattern = Pattern.compile("""$LAT,$LON.*""")
                    parts.dropWhile { it in parseUriParts }.forEachReversed { part ->
                        if (part.startsWith("data=")) {
                            // Data
                            // /data=...!3d44.4490541!4d26.0888398...
                            ("""!3d$LAT!4d$LON""" findPoint part)?.also { points.add(it) }
                            // /data=...!1d13.4236883!2d52.4858222...!1d13.4255518!2d52.4881038...
                            points.addAll("""!1d$LON!2d$LAT""" findAllPoints part)
                        } else if (part.startsWith('@') && centerCoords == null) {
                            // Center
                            // /@52.5067296,13.2599309,6z
                            ("""@$LAT,$LON(,${Z}z)?.*""" matchPoint part)?.let { (lat, lon, z) ->
                                lat?.let { lat ->
                                    lon?.let { lon ->
                                        centerCoords = lat to lon
                                        defaultZ = z
                                    }
                                }
                            }
                        } else {
                            // Coordinates
                            // /52.492611,13.431726
                            part
                                .takeIf { points.isEmpty() } // Only if a point hasn't already been found, e.g. in /data
                                ?.let { pointPattern matchPoint it }
                                ?.also { points.add(it) }
                            // Name
                            // /Central+Park
                                ?: part
                                    .takeIf { defaultName == null } // Use the last name-like path part
                                    ?.let { Q_PATH_PATTERN matchName part }
                                    ?.also { defaultName = it }
                        }
                        // Once we have a point, name, and z, we can stop iterating path parts
                        if (points.isNotEmpty() && defaultName != null && defaultZ != null) {
                            return@forEachReversed
                        }
                    }
                    if (points.isEmpty()) {
                        if (centerCoords != null) {
                            // Use the center point only if we haven't found another point
                            points.add(NaivePoint(centerCoords.first, centerCoords.second))
                        } else if (firstPart !in parseHtmlExcludeParts) {
                            // Go to HTML parsing if needed
                            htmlUriString = uri.toString()
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
                if (points.addAll(pointPattern findAllPoints line)) {
                    log.d("GoogleMapsInput", "HTML Pattern: Point pattern matched line $line")
                }
                if (defaultPoint == null) {
                    (defaultPointLinkPattern findPoint line)?.let {
                        log.d("GoogleMapsInput", "HTML Pattern: Default point pattern 1 matched line $line")
                        defaultPoint = it
                    }
                }
                if (defaultPoint == null && !genericMetaTagFound) {
                    // When the HTML contains a generic "Google Maps" META tag instead of a specific one like
                    // "Berlin - Germany", then it seems that the APP_INITIALIZATION_STATE contains coordinates of the
                    // IP address that the HTTP request came from, instead of correct coordinates. So let's ignore the
                    // coordinates.
                    (defaultPointAppInitStatePattern findPoint line)?.let {
                        log.d("GoogleMapsInput", "HTML Pattern: Default point pattern 2 matched line $line")
                        defaultPoint = it
                    }
                }
                if (redirectUriString == null) {
                    (uriPattern find line)?.groupOrNull("url")?.let {
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
