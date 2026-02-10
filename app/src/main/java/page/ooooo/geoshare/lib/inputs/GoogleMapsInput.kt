package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.ImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
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

                Z_PATTERN.matchEntire(queryParams["zoom"])?.doubleGroupOrNull()?.also { defaultZ = it }

                listOf("origin", "destination", "q", "query", "ll", "viewpoint", "center").forEach { key ->
                    (LAT_LON_PATTERN.matchEntire(queryParams[key])?.toLatLonPoint()
                        ?: Q_PARAM_PATTERN.matchEntire(queryParams[key])?.groupOrNull()?.let { NaivePoint(name = it) })
                        ?.let {
                            points.add(it)
                            if (key != "origin") {
                                if (!it.hasCoordinates()) {
                                    // Go to HTML parsing if needed
                                    htmlUriString = uri.toString()
                                }
                                return@run
                            }
                        }
                }

                // Parse path parts

                val partsThatSupportUriParsing = setOf("dir", "place", "search")
                val partsThatSupportHtmlParsing = setOf(null, "", "@", "d", "dir", "place", "placelists")
                val parts = uri.pathParts.drop(1).dropWhile { it == "maps" }
                val firstPart = parts.firstOrNull()
                if (firstPart in partsThatSupportUriParsing || firstPart?.startsWith('@') == true) {
                    // Iterate path parts in reverse order
                    val pointPattern = Regex("""$LAT,$LON.*""")
                    parts.dropWhile { it in partsThatSupportUriParsing }.forEach { part ->
                        if (part.startsWith("data=")) {
                            // Data
                            // /data=...!3d44.4490541!4d26.0888398...
                            Regex("""!3d$LAT!4d$LON""").find(part)?.toLatLonPoint()?.also {
                                // Overwrite coordinates of previously found points with /data= but copy last point name
                                points.lastOrNull().let { lastPoint ->
                                    points.clear()
                                    points.add(it.copy(name = lastPoint?.name))
                                }
                            } ?:
                            // /data=...!1d13.4236883!2d52.4858222...!1d13.4255518!2d52.4881038...
                            Regex("""!1d$LON!2d$LAT""").findAll(part).mapNotNull { it.toLonLatPoint() }
                                .toList().takeIf { it.isNotEmpty() }?.let {
                                    if (it.size == points.size) {
                                        // Overwrite coordinates of previously found points with /data= but keep names
                                        points.forEachIndexed { i, point ->
                                            points[i] = point.copy(lat = it[i].lat, lon = it[i].lon)
                                        }
                                    } else {
                                        // Overwrite coordinates of previously found points with /data= including names
                                        points.clear()
                                        points.addAll(it)
                                    }
                                }
                        } else if (part.startsWith('@')) {
                            // Center
                            // /@52.5067296,13.2599309,6z
                            Regex("""@$LAT,$LON(?:,${Z}z)?.*""").matchEntire(part)?.toLatLonZPoint()?.also { point ->
                                val lastPoint = points.lastOrNull()
                                if (lastPoint == null) {
                                    // Use center coordinates if we haven't already found a point
                                    points.add(point)
                                } else if (lastPoint.lat == null && lastPoint.lon == null) {
                                    // Use center coordinates with the name of the last found point
                                    points[points.size - 1] = point.copy(name = lastPoint.name)
                                } else {
                                    // Don't use center coordinates if we've already found a point with coordinate
                                }
                                defaultZ = point.z
                            }
                        } else if (part.isNotEmpty()) {
                            // Coordinates
                            // /52.492611,13.431726
                            pointPattern.matchEntire(part)?.toLatLonPoint()?.also { points.add(it) }
                            // Name
                            // /Central+Park
                                ?: points.add(NaivePoint(name = part))
                        }
                    }
                }
                if (points.lastOrNull()?.hasCoordinates() != true && firstPart in partsThatSupportHtmlParsing) {
                    // Go to HTML parsing if needed
                    htmlUriString = uri.toString()
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
                    // "Berlin - Germany", then it seems that the APP_INITIALIZATION_STATE doesn't contain correct
                    // coordinates. It contains coordinates of the IP address that the HTTP request came from. So let's
                    // ignore these coordinates.
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

    override fun shouldInterceptRequest(requestUrlString: String) =
        // Assets
        requestUrlString.endsWith(".gif")
            || requestUrlString.endsWith(".ico")
            || requestUrlString.endsWith(".png")
            || requestUrlString.endsWith(".svg")
            || requestUrlString.contains(@Suppress("SpellCheckingInspection") "fonts.gstatic.com/")
            || requestUrlString.contains(@Suppress("SpellCheckingInspection") "maps.gstatic.com/")
            || requestUrlString.contains(@Suppress("SpellCheckingInspection") "googleusercontent.com/")
            || requestUrlString.contains("/gps-cs-s/")
            || requestUrlString.contains("/ss/")
            || requestUrlString.contains("/thumbnail")

            // Map tiles
            || requestUrlString.contains("/kh/")
            || requestUrlString.contains("/maps/vt")

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
