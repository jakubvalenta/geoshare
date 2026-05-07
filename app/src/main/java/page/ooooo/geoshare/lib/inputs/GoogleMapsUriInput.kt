package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toLatLonZPoint
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source

/**
 * Handles Google Maps full URIs.
 *
 * If the URI contains a Plus Code, e.g. https://www.google.com/maps/place/8FJ3HVHW%2B96, it doesn't process the code
 * but only puts it in the point name. The reason is that Plus Codes are handled by [PlusCodeInput], which has higher
 * priority, so URIs containing Plus Codes should never reach [GoogleMapsUriInput].
 */
object GoogleMapsUriInput : NewUriInput, Input.HasRandomUri {
    override val pattern =
        Regex("""((?:https?://)?(?:(?:www|maps)\.)?google(?:\.[a-z]{2,3})?\.[a-z]{2,3}[/?#]$URI_REST)""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.GOOGLE_MAPS,
        nameResId = R.string.converter_google_maps_name,
        items = listOf(
            InputDocumentationItem.Url(5, "https://maps.google.com"),
            InputDocumentationItem.Url(5, "https://google.com/maps"),
            InputDocumentationItem.Url(5, "https://www.google.com/maps"),
        ),
    )

    override suspend fun parse(uri: Uri, uriQuote: UriQuote) = buildParseResult {
        uri.run {
            val mutableNaivePoints = mutableListOf<NaivePoint>()

            val z = Z_PATTERN.matchEntire(queryParams["zoom"])?.doubleGroupOrNull()

            // API directions
            // https://www.google.com/maps/dir/?origin={lat},{lon}&destination={lat},{lon}
            // https://www.google.com/maps/dir/?origin={name}&destination={name}
            listOf("origin", "destination")
                .mapNotNull { key ->
                    LAT_LON_PATTERN.matchEntire(queryParams[key])?.toLatLonPoint(Source.URI)
                        ?: Q_PARAM_PATTERN.matchEntire(queryParams[key])?.groupOrNull()
                            ?.let { NaivePoint(name = it, source = Source.URI) }
                }
                .takeIf { it.isNotEmpty() }
                ?.let { naivePoints ->
                    mutableNaivePoints.addAll(naivePoints.map { it.copy(z = z) })
                    if (naivePoints.any { !it.hasCoordinates() }) {
                        // Go to HTML parsing unless all points have coordinates
                        nextInput = GoogleMapsHtmlInput
                    }
                    return@run
                }

            // API coordinates
            // https://maps.google.com/?ll={lat},{lon}
            // https://maps.google.com/?q={lat},{lon}
            listOf(
                @Suppress("SpellCheckingInspection") "daddr",
                "q",
                "query",
                "ll",
            )
                .firstNotNullOfOrNull { key ->
                    LAT_LON_PATTERN.matchEntire(queryParams[key])?.toLatLonPoint(Source.URI)
                }?.let {
                    mutableNaivePoints.add(it.copy(z = z))
                    return@run
                }

            // API map center
            // https://maps.google.com/?center={lat},{lon}
            listOf(
                "viewpoint",
                "center",
            )
                .firstNotNullOfOrNull { key ->
                    LAT_LON_PATTERN.matchEntire(queryParams[key])?.toLatLonPoint(Source.MAP_CENTER)
                }?.let {
                    mutableNaivePoints.add(it.copy(z = z))
                    return@run
                }

            // API search
            // https://maps.google.com/?q={name}
            listOf(
                @Suppress("SpellCheckingInspection") "daddr",
                "q",
                "query",
            ).forEach { key ->
                Q_PARAM_PATTERN.matchEntire(queryParams[key])?.groupOrNull()?.let { name ->
                    mutableNaivePoints.add(NaivePoint(z = z, name = name, source = Source.URI))
                    // Go to HTML parsing
                    nextInput = GoogleMapsHtmlInput
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
                        // Data lat-lon points
                        // /data=...!3d{lat}!4d{lon}...!3d{lat}!4d{lon}...
                        (Regex("""!3d$LAT!4d$LON""").findAll(part)
                            .mapNotNull { it.toLatLonPoint(Source.URI) }
                            .toList()
                            .takeIf { it.isNotEmpty() }
                        // Data lon-lat points
                        // /data=...!1d{lon}!2d{lat)...!1d{lon}!2d{lat}...
                            ?: Regex("""!1d$LON!2d$LAT""").findAll(part)
                                .mapNotNull { it.toLonLatPoint(Source.URI) }
                                .toList()
                                .takeIf { it.isNotEmpty() }
                            )?.let { naivePoints ->
                                // Overwrite previously found points, but keep their names
                                if (mutableNaivePoints.size == naivePoints.size) {
                                    mutableNaivePoints.forEachIndexed { i, point ->
                                        mutableNaivePoints[i] = naivePoints[i].copy(z = point.z, name = point.name)
                                    }
                                } else {
                                    // Overwrite previously found points
                                    mutableNaivePoints.clear()
                                    mutableNaivePoints.addAll(naivePoints.map { it.copy(z = z) })
                                }
                                return@forEach
                            }

                    } else if (part.startsWith('@')) {
                        // Map center
                        // /@{lat},{lon},{z}z
                        Regex("""@$LAT,$LON(?:,${Z}z)?.*""").matchEntire(part)?.toLatLonZPoint(Source.MAP_CENTER)
                            ?.let { naivePoint ->
                                val lastPoint = mutableNaivePoints.lastOrNull()
                                if (lastPoint == null) {
                                    // If we haven't already found a point, add center as a new point
                                    mutableNaivePoints.add(naivePoint.let { it.copy(z = it.z ?: z) })
                                } else if (lastPoint.lat == null && lastPoint.lon == null) {
                                    // If we've already found a point, but it has no coordinates, update it with center
                                    mutableNaivePoints[mutableNaivePoints.size - 1] =
                                        naivePoint.let { it.copy(z = it.z ?: lastPoint.z, name = lastPoint.name) }
                                } else {
                                    // If we've already found a pont, and it has coordinates, update it with zoom only
                                    mutableNaivePoints[mutableNaivePoints.size - 1] =
                                        lastPoint.copy(z = naivePoint.z)
                                }
                            }
                    } else if (part.isNotEmpty()) {
                        // Coordinates
                        // /{lat},{lon}
                        pointPattern.matchEntire(part)?.toLatLonPoint(Source.URI)?.let {
                            mutableNaivePoints.add(it.copy(z = z))
                        }
                        // Name or Plus Code
                        // /{name}
                        // https://www.google.com/maps/place/{code}
                            ?: mutableNaivePoints.add(NaivePoint(z = z, name = part, source = Source.URI))
                    }
                }
            }

            if (mutableNaivePoints.lastOrNull()?.hasCoordinates() != true && firstPart in partsThatSupportHtmlParsing) {
                // Go to HTML parsing if needed
                nextInput = GoogleMapsHtmlInput
            }

            points = mutableNaivePoints.map { GCJ02MainlandChinaPoint(it) }.toImmutableList()
        }
    }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(
            point,
            listOf(
                "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                "https://www.google.com/maps/dir/?api=1&destination={lat}%2C{lon}",
                "https://www.google.com/maps/@?api=1&map_action=pano&viewpoint={lat}%2C{lon}",
            ).random(),
        )
}
