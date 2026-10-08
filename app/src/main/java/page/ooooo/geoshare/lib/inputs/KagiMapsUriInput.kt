package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toZLatLonPoint
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KagiMapsUriInput @Inject constructor(
    val openStreetMapApiInput: dagger.Lazy<OpenStreetMapApiInput>,
    val uriQuote: UriQuote,
) : BasicOfflineInput, Input.HasPattern, Input.HasRandomUri {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.KAGI_MAPS
    override val changelog = persistentListOf(
        InputChangelogItem.Url(51, "https://kagi.com/maps"),
    )

    override val pattern = Regex("""((?:https?://)?(?:www\.)?kagi\.com/maps[/?#]$URI_REST)""")

    override fun parse(match: String, resources: Resources) =
        Uri.parse(match, uriQuote).run {
            parseResult {
                // Query
                // https://kagi.com/maps/info?q={q}
                val name = Q_PARAM_PATTERN.matchEntire(queryParams["q"]?.firstOrNull())?.groupOrNull()?.let { q ->
                    q.trim().takeIf { it.isNotEmpty() }
                }

                // Map center from fragment (contains zoom)
                // https://kagi.com/maps/#{z}/{lat}/{lon}
                val centerFromFragment = Regex("""$Z/$LAT/$LON""").matchEntire(fragment)
                    ?.toZLatLonPoint(Source.MAP_CENTER)

                // Zoom (takes precedence over map center zoom)
                // https://kagi.com/maps/info?z={z}
                val z =
                    Z_PATTERN.matchEntire(queryParams["z"]?.firstOrNull())?.doubleGroupOrNull() ?: centerFromFragment?.z

                queryParams["id"]?.firstOrNull()?.takeIf { it.isNotEmpty() }?.let { id ->
                    when (id.firstOrNull()) {
                        'p' ->
                            // Point
                            // https://kagi.com/maps/info?id=point_{lat}_{lon}
                            Regex("""point_${LAT}_${LON}""").matchEntire(id)
                                ?.toLatLonPoint(source = Source.URI)
                                ?.let {
                                    points = persistentListOf(WGS84Point(it, z = z, name = name))
                                }

                        'n' ->
                            // OSM node
                            // https://kagi.com/maps/info?id=n{osmId}
                            id.substring(1).toLongOrNull()?.let { osmId ->
                                points = persistentListOf(WGS84Point(z = z, name = name, source = Source.URI))
                                next = MatchedInput(
                                    openStreetMapApiInput.get(),
                                    OpenStreetMapApiInput.formatUrlString(
                                        OpenStreetMapApiInput.Companion.ElementType.NODE, osmId
                                    ),
                                )
                            }

                        'r' ->
                            // OSM relation
                            // https://kagi.com/maps/info?id=r{osmId}
                            id.substring(1).toLongOrNull()?.let { osmId ->
                                points = persistentListOf(WGS84Point(z = z, name = name, source = Source.URI))
                                next = MatchedInput(
                                    openStreetMapApiInput.get(),
                                    OpenStreetMapApiInput.formatUrlString(
                                        OpenStreetMapApiInput.Companion.ElementType.RELATION, osmId
                                    ),
                                )
                            }

                        'w' ->
                            // OSM way
                            // https://kagi.com/maps/info?id=w{osmId}
                            id.substring(1).toLongOrNull()?.let { osmId ->
                                points = persistentListOf(WGS84Point(z = z, name = name, source = Source.URI))
                                next = MatchedInput(
                                    openStreetMapApiInput.get(),
                                    OpenStreetMapApiInput.formatUrlString(
                                        OpenStreetMapApiInput.Companion.ElementType.WAY, osmId
                                    ),
                                )
                            }

                        else -> {
                            // Opaque id (not supported and don't use map center, because user expects the point location)
                            // https://kagi.com/maps/info?id={id}
                            warningMessage = resources.getString(R.string.input_kagi_maps_warning_account)
                        }
                    }
                    return@parseResult
                }

                // Directions
                // https://kagi.com/maps/directions?q={point1Name}~{point1Lat}%2C{point1Lon}|{point2Lat}%2C{point2Lon}|...
                if (pathParts.getOrNull(2) == "directions") {
                    queryParams["q"]?.firstOrNull()?.let { q ->
                        q
                            .split('|')
                            .filter { it.isNotBlank() }
                            .map { pointStr ->
                                val nameAndCoordinates = pointStr.split('~', limit = 2)
                                val name = nameAndCoordinates.firstOrNull()
                                val coordinates = nameAndCoordinates.lastOrNull()
                                // Notice that name and coordinates can be the same list element
                                LAT_LON_PATTERN.matchEntire(coordinates)?.toLatLonPoint(Source.URI)?.let {
                                    WGS84Point(it, z = z, name = name.takeIf { name -> name != coordinates })
                                } ?: WGS84Point(name = name, source = Source.URI)
                            }
                            .toImmutableList()
                            .let {
                                points = it
                                return@parseResult
                            }
                    }
                }

                // Map center from query parameter (takes precedence over center from fragment)
                // https://kagi.com/maps/info?ll={lat}%2C{lon}
                (LAT_LON_PATTERN.matchEntire(queryParams["ll"]?.firstOrNull())?.toLatLonPoint(Source.MAP_CENTER)
                    ?: centerFromFragment)?.let {
                    points = persistentListOf(WGS84Point(it, z = z, name = name))
                    return@parseResult
                }

                if (name != null) {
                    points = persistentListOf(WGS84Point(name = name, source = Source.URI))
                }
            }
        }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(
            point,
            "https://kagi.com/maps/info?q={name}&id=point_{lat}_{lon}&ll={lat}%2C{lon}#{z}/{lat}/{lon}"
        )

    override fun toString() = "KagiMapsUriInput"
}
