package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toZLatLonPoint
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KagiMapsUriInput @Inject constructor(
    val openStreetMapApiInput: OpenStreetMapApiInput,
    override val uriQuote: UriQuote,
) : UriInput, Input.HasRandomUri {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.KAGI_MAPS
    override val changelog = persistentListOf(
        InputChangelogItem.Url(51, "https://kagi.com/maps"),
    )

    override val pattern = Regex("""((?:https?://)?(?:www\.)?kagi\.com/maps[/?#]$URI_REST)""")

    override suspend fun parse(data: Uri, match: String, resources: Resources) = parseResult {
        data.run {
            // Query
            // https://kagi.com/maps/info?q={q}
            val name = Q_PARAM_PATTERN.matchEntire(queryParams["q"])?.groupOrNull()

            // Map center
            // https://kagi.com/maps/info?ll={lat}%2C{lon}
            // https://kagi.com/maps/info#{z}/{lat}/{lon}
            val center = Regex("""$Z/$LAT/$LON""").matchEntire(fragment)?.toZLatLonPoint(Source.MAP_CENTER)
                ?: LAT_LON_PATTERN.matchEntire(queryParams["ll"])?.toLatLonPoint(Source.MAP_CENTER)
            val z = center?.z

            queryParams["id"]?.let { id ->
                // Coordinates
                // https://kagi.com/maps/info?id=point_{lat}_{lon}
                Regex("""point_${LAT}_${LON}""").matchEntire(id)
                    ?.toLatLonPoint(source = Source.URI)?.let {
                        points = persistentListOf(WGS84Point(it, z = z, name = name))
                        return@parseResult
                    }

                // OSM id
                // https://kagi.com/maps/info?id=n{osmId}
                // https://kagi.com/maps/info?id=r{osmId}
                // https://kagi.com/maps/info?id=w{osmId}
                Regex("""([nrw])(\d+)""").matchEntire(id)?.let { m ->
                    val prefix = m.groupOrNull(1)
                    val osmId = m.groupOrNull(2)
                    val type = when(prefix) {
                        "n" -> "node"
                        "r" -> "relation"
                        "w" -> "way"
                        else -> null
                    }
                    if (type != null) {
                        if (center != null) {
                            // Use center as fallback if the user later denies OpenStreetMap API permission
                            points = persistentListOf(WGS84Point(center, name = name))
                        }
                        next = MatchedInput(openStreetMapApiInput, "https://www.openstreetmap.org/$type/$osmId")
                        return@parseResult
                    }
                }

                // Opaque id (not supported)
                // https://kagi.com/maps/info?id={id}
            }

            // Directions
            // https://kagi.com/maps/directions?q={point1Name}~{point1Lat}%2C{point1Lon}|{point2Lat}%2C{point2Lon}|...
            if (pathParts.firstOrNull() == "" && pathParts.getOrNull(1) == "directions") {
                queryParams["q"]
                    ?.takeIf { it.isNotBlank() }
                    ?.split('|')
                    ?.map { pointStr ->
                        val nameAndCoordinates = pointStr.split('~', limit = 1)
                        val name = nameAndCoordinates.firstOrNull()
                        val coordinates = nameAndCoordinates.lastOrNull()
                        // Notice that name and coordinates can be the same list element
                        LAT_LON_PATTERN.matchEntire(coordinates)?.toLatLonPoint(Source.URI)?.let {
                            WGS84Point(it, z = z, name = name.takeIf { name -> name != coordinates })
                        } ?: WGS84Point(name = name, source = Source.URI)
                    }
                    ?.toImmutableList()
                    ?.let {
                        points = it
                        return@parseResult
                    }
            }

            if (center != null) {
                points = persistentListOf(WGS84Point(center, name = name))
            } else if (name != null) {
                points = persistentListOf(WGS84Point(name = name, source = Source.URI))
            }
        }
    }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "https://kagi.com/maps/info?q={name}&id=point_{lat}_{lon}&ll={lat}%2C{lon}#{z}/{lat}/{lon}")

    override fun toString() = "KagiMapsUriInput"
}
