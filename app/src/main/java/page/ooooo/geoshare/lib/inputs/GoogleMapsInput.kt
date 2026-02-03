package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.ImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.forEachReversed
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toLatLonZPoint
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.asGCJ02
import page.ooooo.geoshare.lib.point.buildPoints

object GoogleMapsInput : Input.HasShortUri, Input.HasHtml, Input.HasWeb {
    private const val TAG = "GoogleMapsInput"
    private const val SHORT_URL = """(?:(?:maps\.)?(?:app\.)?goo\.gl|g\.co)/[/A-Za-z0-9_-]+"""

    override val uriPattern =
        Regex("""(?:https?://)?(?:(?:www|maps)\.)?(?:google(?:\.[a-z]{2,3})?\.[a-z]{2,3}[/?#]\S+|$SHORT_URL)""")
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
    override val shortUriPattern = Regex("""(?:https?://)?$SHORT_URL""")
    override val shortUriMethod = Input.ShortUriMethod.HEAD

    override suspend fun parseUri(uri: Uri) = buildParseUriResult {
        points = buildPoints {
            uri.run {
                // Try query parameters for all URLs

                listOf("destination", "q", "query", "ll", "viewpoint", "center")
                    .firstNotNullOfOrNull { key -> LAT_LON_PATTERN.matchEntire(queryParams[key]) }
                    ?.toLatLonPoint()
                    ?.also { points.add(it) }

                listOf("destination", "q", "query")
                    .firstNotNullOfOrNull { key -> Q_PARAM_PATTERN.matchEntire(queryParams[key]) }
                    ?.groupOrNull()
                    ?.also { defaultName = it }

                Z_PATTERN.matchEntire(queryParams["zoom"])?.doubleGroupOrNull()?.also { defaultZ = it }

                // Parse path parts

                val partsThatSupportUriParsing = setOf("dir", "place", "search")
                val partsThatSupportHtmlParsing = setOf(null, "", "@", "d", "dir", "place", "placelists")
                val partsThatSupportWebParsing = setOf("place")
                val parts = uri.pathParts.drop(1).dropWhile { it == "maps" }
                val firstPart = parts.firstOrNull()
                if (firstPart in partsThatSupportUriParsing || firstPart?.startsWith('@') == true) {
                    // Iterate path parts in reverse order
                    var centerCoords: Pair<Double, Double>? = null
                    val pointPattern = Regex("""$LAT,$LON.*""")
                    parts.dropWhile { it in partsThatSupportUriParsing }.forEachReversed { part ->
                        if (part.startsWith("data=")) {
                            // Data
                            // /data=...!3d44.4490541!4d26.0888398...
                            Regex("""!3d$LAT!4d$LON""").find(part)?.toLatLonPoint()?.also { points.add(it) }
                            // /data=...!1d13.4236883!2d52.4858222...!1d13.4255518!2d52.4881038...
                            points.addAll((Regex("""!1d$LON!2d$LAT""").findAll(part)).mapNotNull { it.toLonLatPoint() })
                        } else if (part.startsWith('@') && centerCoords == null) {
                            // Center
                            // /@52.5067296,13.2599309,6z
                            Regex("""@$LAT,$LON(?:,${Z}z)?.*""").matchEntire(part)?.toLatLonZPoint()?.let { point ->
                                if (point.lat != null && point.lon != null) {
                                    centerCoords = point.lat to point.lon
                                }
                                defaultZ = point.z
                            }
                        } else {
                            // Coordinates
                            // /52.492611,13.431726
                            part
                                .takeIf { points.isEmpty() } // Only if a point hasn't already been found, e.g. in /data
                                ?.let { pointPattern.matchEntire(it) }
                                ?.toLatLonPoint()
                                ?.also { points.add(it) }
                            // Name
                            // /Central+Park
                                ?: part
                                    .takeIf { defaultName == null } // Use the last name-like path part
                                    ?.let { Q_PATH_PATTERN.matchEntire(part) }
                                    ?.groupOrNull()
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
                        } else if (firstPart in partsThatSupportHtmlParsing) {
                            // Go to HTML parsing if needed
                            htmlUriString = uri.toString()
                        }
                    }
                } else if (firstPart in partsThatSupportHtmlParsing) {
                    // Go to HTML parsing if needed
                    htmlUriString = uri.toString()
                } else if (firstPart in partsThatSupportWebParsing) {
                    // Go to web parsing
                    webUriString = uri.toString()
                }
            }
        }.asGCJ02()
    }

    override suspend fun parseHtml(
        htmlUrlString: String,
        channel: ByteReadChannel,
        pointsFromUri: ImmutableList<Point>,
        log: ILog,
    ) = buildParseHtmlResult {
        points = buildPoints {
            defaultName = pointsFromUri.lastOrNull()?.name

            var defaultPoint: NaivePoint? = null
            var genericMetaTagFound = false
            val pointPattern = Regex("""\[(?:null,null,|null,\[)$LAT,$LON]""")
            val defaultPointLinkPattern = Regex("""/@$LAT,$LON""")
            val defaultPointAppInitStatePattern =
                Regex("""APP_INITIALIZATION_STATE=\[\[\[[\d.-]+,$LON,$LAT""")
            val genericMetaTagPattern = Regex(
                @Suppress("SpellCheckingInspection") """<meta content="Google Maps" itemprop="name""""
            )
            val uriPattern = Regex("""data-url="([^"]+)"""")

            while (true) {
                val line = channel.readLine() ?: break
                if (!genericMetaTagFound && genericMetaTagPattern.find(line) != null) {
                    log.d("GoogleMapsInput", "HTML Pattern: Generic meta tag matched line $line")
                    genericMetaTagFound = true
                }
                if (points.addAll((pointPattern.findAll(line).mapNotNull { it.toLatLonPoint() }))) {
                    log.d("GoogleMapsInput", "HTML Pattern: Point pattern matched line $line")
                }
                if (defaultPoint == null) {
                    defaultPointLinkPattern.find(line)?.toLatLonPoint()?.let {
                        log.d("GoogleMapsInput", "HTML Pattern: Default point pattern 1 matched line $line")
                        defaultPoint = it
                    }
                }
                if (defaultPoint == null && !genericMetaTagFound) {
                    // When the HTML contains a generic "Google Maps" META tag instead of a specific one like
                    // "Berlin - Germany", then it seems that the APP_INITIALIZATION_STATE contains coordinates of the
                    // IP address that the HTTP request came from, instead of correct coordinates. So let's ignore the
                    // coordinates.
                    defaultPointAppInitStatePattern.find(line)?.toLonLatPoint()?.let {
                        log.d("GoogleMapsInput", "HTML Pattern: Default point pattern 2 matched line $line")
                        defaultPoint = it
                    }
                }
                if (redirectUriString == null) {
                    uriPattern.find(line)?.groupOrNull()?.let {
                        log.d("GoogleMapsInput", "HTML Pattern: URI pattern matched line $line")
                        redirectUriString = it
                    }
                }
            }

            if (points.isEmpty()) {
                if (defaultPoint != null) {
                    points.add(defaultPoint)
                } else {
                    webUriString = htmlUrlString
                }
            }
        }.asGCJ02()
    }

    override suspend fun onUrlChange(
        urlString: String,
        pointsFromUri: ImmutableList<Point>,
        log: ILog,
    ) = buildParseWebResult {
        val parseUriResult = parseUri(Uri.parse(urlString))
        if (parseUriResult is ParseUriResult.Succeeded) {
            log.i(TAG, "Parsed web URL $urlString to ${parseUriResult.points}")
            points = parseUriResult.points
        } else {
            log.i(TAG, "Failed to parse web URL $urlString")
        }
    }

    override fun shouldInterceptRequest(requestUrlString: String, log: ILog) =
        // Assets
        requestUrlString.contains(".css")
            || requestUrlString.endsWith(".gif")
            || requestUrlString.endsWith(".ico")
            || requestUrlString.endsWith(".png")
            || requestUrlString.endsWith(".svg")
            || requestUrlString.startsWith("https://fonts.gstatic.com/")
            || requestUrlString.startsWith("https://maps.gstatic.com/")

            // Map tiles
            || requestUrlString.startsWith("https://khms")
            || requestUrlString.startsWith("https://www.google.com/maps/vt/")

            // Tracking
            || requestUrlString.contains("/generate_204")
            || requestUrlString.contains("/log204")
            || requestUrlString.contains("google.com/gen_204")
            || requestUrlString.contains("google.com/log")
            || requestUrlString.contains(@Suppress("SpellCheckingInspection") "googlesyndication.com/")

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
}
