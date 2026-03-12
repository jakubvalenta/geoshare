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
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.prefixedHexToLongOrNull
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toLatLonZPoint
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.geo.decodeS2CellId
import page.ooooo.geoshare.lib.point.GCJ02Point
import page.ooooo.geoshare.lib.point.Point

object GoogleMapsInput : ShortUriInput, HtmlInput, WebInput, Input.HasRandomUri {
    private const val SHORT_URL = """(?:(?:maps\.)?(?:app\.)?goo\.gl|g\.co)/[/A-Za-z0-9_-]+"""
    private const val HEX = """(0x[A-Fa-f0-9]+)"""

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

    override suspend fun parseUri(uri: Uri) = buildParseUriResult {
        uri.run {
            val mutablePoints = mutableListOf<Point>()

            var z = Z_PATTERN.matchEntire(queryParams["zoom"])?.doubleGroupOrNull()

            // Try query parameters for all URLs

            listOf("origin", "destination", "q", "query", "ll", "viewpoint", "center").forEach { key ->
                LAT_LON_PATTERN.matchEntire(queryParams[key])?.toLatLonPoint()
                    ?.also {
                        mutablePoints.add(it.asGCJ02().copy(z = z))
                    }
                    ?: Q_PARAM_PATTERN.matchEntire(queryParams[key])?.groupOrNull()?.also { name ->
                        // TODO Feels wrong
                        points = persistentListOf(GCJ02Point(z = z, name = name))
                        if (key != "origin") {
                            // Go to HTML parsing if needed
                            htmlUriString = uri.toString()
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
                // Iterate path parts
                val pointPattern = Regex("""$LAT,$LON.*""")
                parts.dropWhile { it in partsThatSupportUriParsing }.forEach { part ->
                    if (part.startsWith("data=")) {
                        // Data
                        // /data=...!1s0x47a84fb831937021:0x28d6914e5ca0f9f5...
                        Regex("""!1s$HEX:""").findAll(part)
                            .mapNotNull { it.groupOrNull(1)?.prefixedHexToLongOrNull() }
                            .map { id -> decodeS2CellId(id).asWGS84() }
                            .toList()
                            .takeIf { it.isNotEmpty() }
                            ?.let {
                                // TODO
                                return@forEach
                            }

                        // /data=...!3d44.4490541!4d26.0888398...
                        Regex("""!3d$LAT!4d$LON""").find(part)?.toLatLonPoint()
                            ?.asGCJ02()
                            ?.let {
                                // TODO
                                return@forEach
                            }

                        // /data=...!1d13.4236883!2d52.4858222...!1d13.4255518!2d52.4881038...
                        Regex("""!1d$LON!2d$LAT""").findAll(part)
                            .mapNotNull { it.toLonLatPoint()?.asGCJ02() }
                            .toList()
                            .takeIf { it.isNotEmpty() }
                            ?.let {
                                if (it.size == mutablePoints.size) {
                                    // Overwrite coordinates of previously found points with /data= but keep names
                                    mutablePoints.forEachIndexed { i, point ->
                                        mutablePoints[i] =
                                            GCJ02Point(lat = it[i].lat, lon = it[i].lon, z = point.z, name = point.name)
                                    }
                                } else {
                                    // Overwrite coordinates of previously found points with /data= including names
                                    mutablePoints.clear()
                                    mutablePoints.addAll(it)
                                }
                                return@forEach
                            }
                    } else if (part.startsWith('@')) {
                        // Center
                        // /@52.5067296,13.2599309,6z
                        Regex("""@$LAT,$LON(?:,${Z}z)?.*""").matchEntire(part)?.toLatLonZPoint()?.also {
                            val lastPoint = mutablePoints.lastOrNull()
                            if (lastPoint == null) {
                                // Use center coordinates if we haven't already found a point
                                mutablePoints.add(it.asGCJ02())
                            } else if (lastPoint.lat == null && lastPoint.lon == null) {
                                // Use center coordinates with the name of the last found point
                                mutablePoints[mutablePoints.size - 1] = it.asGCJ02().copy(name = lastPoint.name)
                            } else {
                                // Don't use center coordinates if we've already found a point with coordinates
                            }
                            z = it.z
                        }
                    } else if (part.isNotEmpty()) {
                        // Coordinates
                        // /52.492611,13.431726
                        pointPattern.matchEntire(part)?.toLatLonPoint()?.also {
                            mutablePoints.add(it.asGCJ02())
                        }
                        // Name
                        // /Central+Park
                            ?: mutablePoints.add(GCJ02Point(name = part))
                    }
                }
            }

            // TODO Set last point z

            if (mutablePoints.lastOrNull()?.hasCoordinates() != true && firstPart in partsThatSupportHtmlParsing) {
                // Go to HTML parsing if needed
                htmlUriString = uri.toString()
            }
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
            if (mutablePoints.addAll((pointPattern.findAll(line).mapNotNull { it.toLatLonPoint()?.asGCJ02() }))) {
                log.d("GoogleMapsInput", "HTML Pattern: Point pattern matched line $line")
            }
            if (defaultPoint == null) {
                defaultPointLinkPattern.find(line)?.toLatLonPoint()?.asGCJ02()?.let {
                    log.d("GoogleMapsInput", "HTML Pattern: Default point pattern 1 matched line $line")
                    defaultPoint = it
                }
            }
            if (defaultPoint == null && !genericMetaTagFound) {
                // When the HTML contains a generic "Google Maps" META tag instead of a specific one like
                // "Berlin - Germany", then it seems that the APP_INITIALIZATION_STATE doesn't contain correct
                // coordinates. It contains coordinates of the IP address that the HTTP request came from. So let's
                // ignore these coordinates.
                defaultPointAppInitStatePattern.find(line)?.toLonLatPoint()?.asGCJ02()?.let {
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

        if (mutablePoints.isEmpty()) {
            if (defaultPoint != null) {
                mutablePoints.add(defaultPoint)
            } else {
                webUriString = htmlUrlString
            }
        }

        // TODO Set last point name

        points = mutablePoints.toImmutableList()
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
