package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toLatLonZPoint
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.Source

data class GoogleMapsParseResult(
    val points: Points = persistentListOf(),
    val isPlaceList: Boolean = false,
    val requiresHtmlParsing: Boolean = false,
)

class GoogleMapsParseResultScope {
    var points: Points = persistentListOf()
    var isPlaceList: Boolean = false
    var requiresHtmlParsing: Boolean = false

    internal fun build() = GoogleMapsParseResult(
        points = points,
        isPlaceList = isPlaceList,
        requiresHtmlParsing = requiresHtmlParsing,
    )
}

fun googleMapsParseResult(block: GoogleMapsParseResultScope.() -> Unit): GoogleMapsParseResult =
    GoogleMapsParseResultScope().apply { this.block() }.build()

object GoogleMapsUriParser {
    fun parse(uri: Uri): GoogleMapsParseResult = googleMapsParseResult {
        uri.run {
            val z = Z_PATTERN.matchEntire(queryParams["zoom"])?.doubleGroupOrNull()

            // API directions
            // https://www.google.com/maps/dir/?origin={lat},{lon}&destination={lat},{lon}
            // https://www.google.com/maps/dir/?origin={name}&destination={name}
            listOf(
                "origin",
                "destination",
            )
                .mapNotNull { key ->
                    LAT_LON_PATTERN.matchEntire(queryParams[key])?.toLatLonPoint(Source.URI)
                        ?: Q_PARAM_PATTERN.matchEntire(queryParams[key])?.groupOrNull()
                            ?.let { NaivePoint(name = it, source = Source.URI) }
                }
                .takeIf { it.isNotEmpty() }
                ?.let { naivePoints ->
                    points = naivePoints.map { GCJ02MainlandChinaPoint(it, z) }.toImmutableList()
                    return@googleMapsParseResult
                }

            // API coordinates
            // https://maps.google.com/?ll={lat},{lon}
            // https://maps.google.com/?q={lat},{lon}
            listOf(
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "daddr",
                "q",
                "query",
                "ll",
            )
                .firstNotNullOfOrNull { key ->
                    LAT_LON_PATTERN.matchEntire(queryParams[key])?.toLatLonPoint(Source.URI)
                }?.let {
                    points = persistentListOf(GCJ02MainlandChinaPoint(it, z))
                    return@googleMapsParseResult
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
                    points = persistentListOf(GCJ02MainlandChinaPoint(it, z))
                    return@googleMapsParseResult
                }

            // API search
            // https://maps.google.com/?q={name}
            // https://maps.google.com/?q={name}&query_place_id={name}
            // https://maps.google.com/?query_place_id={name}
            val query = listOf(
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "daddr",
                "q",
                "query",
            )
                .firstNotNullOfOrNull { key -> Q_PARAM_PATTERN.matchEntire(queryParams[key])?.groupOrNull() }
            val placeId = Q_PARAM_PATTERN.matchEntire(queryParams["query_place_id"])?.groupOrNull()
            if (query != null || placeId != null) {
                points = persistentListOf(
                    GCJ02MainlandChinaPoint(z = z, name = query, placeId = placeId, source = Source.URI)
                )
                return@googleMapsParseResult
            }

            val parts = pathParts.dropWhile { it.isEmpty() || it == "maps" }
            val firstPart = parts.firstOrNull()
            when {
                // Empty
                firstPart == null || firstPart == "" -> {}

                // Directions
                // https://www.google.com/maps/place/{point}/{point}/@{centerX},{centerY},{centerZ}
                // Place
                // https://www.google.com/maps/place/{name}/@{centerX},{centerY},{centerZ}
                // Search
                // https://www.google.com/maps/search/{query}
                firstPart == "dir" || firstPart == "place" || firstPart == "search" -> {
                    points = parseParts(parts.drop(1), z)
                }

                // Place list
                // https://www.google.com/maps/placelists/list/{id}
                // https://www.google.com/maps/@/data=!3m1!4b1!...!2s{id}
                // https://www.google.com/maps/d/edit?mid={id}
                // https://www.google.com/maps/d/view?mid={id}
                firstPart == "placelists" || firstPart == "@" || firstPart == "d" -> {
                    isPlaceList = true
                }

                // Map center
                // https://www.google.com/maps/@{centerX},{centerY},{centerZ}
                // Street view
                // https://www.google.com/maps/@{centerX},{centerY},...
                firstPart.startsWith('@') -> {
                    points = parseParts(parts, z)
                }
            }

            // ID
            // https://maps.google.com/?ftid={id}
            if (
                points.lastOrNull()?.hasCoordinates() != true &&
                !queryParams[@Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "ftid"].isNullOrEmpty()
            ) {
                requiresHtmlParsing = true
            }
        }
    }

    private fun parseParts(parts: List<String>, z: Double?): Points {
        val pointPattern = Regex("""$LAT$COORD_SEP$LON.*""")

        val mutableNaivePoints = mutableListOf<NaivePoint>()

        parts.forEach { part ->
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
                Regex("""@$LAT$COORD_SEP$LON(?:,${Z}z)?.*""").matchEntire(part)?.toLatLonZPoint(Source.MAP_CENTER)
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
                            // If we've already found a point, and it has coordinates, update it with zoom only
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

        return mutableNaivePoints.map { GCJ02MainlandChinaPoint(it) }.toImmutableList()
    }
}
