package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toLatLonZPoint
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.point.GCJ02Point
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.Point

object GoogleMapsInput : ShortUriInput, HtmlInput, WebInput, Input.HasRandomUri {
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
    override val shortUriMethod = ShortUriInput.Method.HEAD

    override suspend fun parseUri(uri: Uri, uriQuote: UriQuote): ParseUriResult = buildParseUriResult {
        // Google Maps Go
        // https://maps.app.goo.gl/?link={url}
        val cleanUri = uri.queryParams["link"]?.takeIf { it.isNotEmpty() }?.let { Uri.parse(it, uriQuote) }
            ?: uri

        cleanUri.run {
            val mutablePoints = mutableListOf<Point>()

            val z = Z_PATTERN.matchEntire(queryParams["zoom"])?.doubleGroupOrNull()

            // API directions
            // https://www.google.com/maps/dir/?origin={lat},{lon}&destination={lat},{lon}
            // https://www.google.com/maps/dir/?origin={name}&destination={name}
            listOf("origin", "destination")
                .mapNotNull { key ->
                    LAT_LON_PATTERN.matchEntire(queryParams[key])?.toLatLonPoint()
                        ?: Q_PARAM_PATTERN.matchEntire(queryParams[key])?.groupOrNull()?.let { NaivePoint(name = it) }
                }
                .takeIf { it.isNotEmpty() }
                ?.let { naivePoints ->
                    points = naivePoints.map { it.asGCJ02().copy(z = z) }.toImmutableList()
                    if (points.any { !it.hasCoordinates() }) {
                        // Go to HTML parsing if needed
                        htmlUriString = toString()
                    }
                    return@run
                }

            // API coordinates
            // https://maps.google.com/?ll={lat},{lon}
            // https://maps.google.com/?q={lat},{lon}
            listOf("q", "query", "ll", "viewpoint", "center")
                .firstNotNullOfOrNull { key -> LAT_LON_PATTERN.matchEntire(queryParams[key])?.toLatLonPoint() }?.let {
                    points = persistentListOf(it.asGCJ02().copy(z = z))
                    return@run
                }

            // API search
            // https://maps.google.com/?q={name}
            listOf("q", "query", "ll", "viewpoint", "center").forEach { key ->
                Q_PARAM_PATTERN.matchEntire(queryParams[key])?.groupOrNull()?.let { name ->
                    points = persistentListOf(GCJ02Point(z = z, name = name))
                    // Go to HTML parsing if needed
                    htmlUriString = toString()
                    return@run
                }
            }

            val partsThatSupportUriParsing = setOf("dir", "place", "search")
            val partsThatSupportHtmlParsing = setOf(null, "", "@", "d", "dir", "place", "placelists")
            val parts = pathParts.drop(1).dropWhile { it.isEmpty() || it == "maps" }
            val firstPart = parts.firstOrNull()
            if (firstPart in partsThatSupportUriParsing || firstPart?.startsWith('@') == true) {
                // Iterate path parts
                val pointPattern = Regex("""$LAT,$LON.*""")
                parts.dropWhile { it in partsThatSupportUriParsing }.forEach { part ->
                    if (part.startsWith("data=")) {
                        // Data one point
                        // /data=...!3d{lat}!4d{lon}...
                        Regex("""!3d$LAT!4d$LON""").find(part)?.toLatLonPoint()
                            ?.let { naivePoint ->
                                // Overwrite previously found points, but copy last point name
                                mutablePoints.lastOrNull().let { lastPoint ->
                                    mutablePoints.clear()
                                    mutablePoints.add(
                                        naivePoint.asGCJ02().copy(z = lastPoint?.z, name = lastPoint?.name)
                                    )
                                }
                                return@forEach
                            }

                        // Data several points
                        // /data=...!1d{lon}!2d{lat)...!1d{lon}!2d{lat}...
                        Regex("""!1d$LON!2d$LAT""").findAll(part)
                            .mapNotNull { it.toLonLatPoint() }
                            .toList()
                            .takeIf { it.isNotEmpty() }
                            ?.let { naivePoints ->
                                // Overwrite previously found points, but keep their names
                                if (mutablePoints.size == naivePoints.size) {
                                    mutablePoints.forEachIndexed { i, point ->
                                        mutablePoints[i] = naivePoints[i].asGCJ02().copy(
                                            z = point.z,
                                            name = point.name,
                                        )
                                    }
                                } else {
                                    // Overwrite previously found points
                                    mutablePoints.clear()
                                    mutablePoints.addAll(naivePoints.map { it.asGCJ02().copy(z = z) })
                                }
                                return@forEach
                            }

                    } else if (part.startsWith('@')) {
                        // Center
                        // /@{lat},{lon},{z}z
                        Regex("""@$LAT,$LON(?:,${Z}z)?.*""").matchEntire(part)?.toLatLonZPoint()?.let { naivePoint ->
                            val lastPoint = mutablePoints.lastOrNull()
                            if (lastPoint == null) {
                                // If we haven't already found a point, add center as a new point
                                mutablePoints.add(naivePoint.asGCJ02().let { it.copy(z = it.z ?: z) })
                            } else if (lastPoint.lat == null && lastPoint.lon == null) {
                                // If we've already found a point, but it has no coordinates, update it with center
                                mutablePoints[mutablePoints.size - 1] =
                                    naivePoint.asGCJ02().let { it.copy(z = it.z ?: lastPoint.z, name = lastPoint.name) }
                            } else {
                                // If we've already found a pont, and it has coordinates, update it with zoom only
                                mutablePoints[mutablePoints.size - 1] = lastPoint.toGCJ02().copy(z = naivePoint.z)
                            }
                        }
                    } else if (part.isNotEmpty()) {
                        // Coordinates
                        // /{lat},{lon}
                        pointPattern.matchEntire(part)?.toLatLonPoint()?.let {
                            mutablePoints.add(it.asGCJ02().copy(z = z))
                        }
                        // Name
                        // /{name}
                            ?: mutablePoints.add(GCJ02Point(z = z, name = part))
                    }
                }
            }

            if (mutablePoints.lastOrNull()?.hasCoordinates() != true && firstPart in partsThatSupportHtmlParsing) {
                // Go to HTML parsing if needed
                htmlUriString = toString()
            }

            points = mutablePoints.toImmutableList()
        }
    }


    override suspend fun parseHtml(
        htmlUrlString: String,
        channel: ByteReadChannel,
        pointsFromUri: ImmutableList<Point>,
        log: ILog,
    ) = buildParseHtmlResult {
        val name = pointsFromUri.lastOrNull()?.name
        val mutablePoints = mutableListOf<GCJ02Point>()
        var defaultPoint: GCJ02Point? = null

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
            if (
                mutablePoints.addAll(
                    pointPattern.findAll(line).mapNotNull { it.toLatLonPoint()?.asGCJ02()?.copy(name = name) }
                )
            ) {
                log.d("GoogleMapsInput", "HTML Pattern: Point pattern matched line $line")
            }
            if (defaultPoint == null) {
                defaultPointLinkPattern.find(line)?.toLatLonPoint()?.let {
                    log.d("GoogleMapsInput", "HTML Pattern: Default point pattern 1 matched line $line")
                    defaultPoint = it.asGCJ02().copy(name = name)
                }
            }
            if (defaultPoint == null && !genericMetaTagFound) {
                // When the HTML contains a generic "Google Maps" META tag instead of a specific one like
                // "Berlin - Germany", then it seems that the APP_INITIALIZATION_STATE doesn't contain correct
                // coordinates. It contains coordinates of the IP address that the HTTP request came from. So let's
                // ignore these coordinates.
                defaultPointAppInitStatePattern.find(line)?.toLonLatPoint()?.let {
                    log.d("GoogleMapsInput", "HTML Pattern: Default point pattern 2 matched line $line")
                    defaultPoint = it.asGCJ02().copy(name = name)
                }
            }
            if (redirectUriString == null) {
                uriPattern.find(line)?.groupOrNull()?.let {
                    log.d("GoogleMapsInput", "HTML Pattern: URI pattern matched line $line")
                    redirectUriString = it
                }
            }
        }

        if (mutablePoints.isNotEmpty()) {
            points = mutablePoints.toImmutableList()
        } else if (defaultPoint != null) {
            points = persistentListOf(defaultPoint)
        } else {
            // Go to web parsing
            webUriString = htmlUrlString
        }
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

    override fun genRandomUri(point: Point) =
        point.formatUriString(
            listOf(
                "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                "https://www.google.com/maps/dir/?api=1&destination={lat}%2C{lon}",
                "https://www.google.com/maps/@?api=1&map_action=pano&viewpoint={lat}%2C{lon}",
            ).random()
        )
}
