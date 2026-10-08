package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toNamePoint
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppleMapsUriInput @Inject constructor(
    private val appleMapsHtmlInput: dagger.Lazy<AppleMapsHtmlInput>,
    val uriQuote: UriQuote,
) : BasicOfflineInput, Input.HasPattern, Input.HasRandomUri {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.APPLE_MAPS
    override val changelog = persistentListOf(
        InputChangelogItem.Url(18, "https://maps.apple"),
        InputChangelogItem.Url(18, "https://maps.apple.com"),
    )

    override val pattern = Regex("""((?:https?://)?maps\.apple(\.com)?[/?#]$URI_REST)""")

    override fun parse(match: String, resources: Resources) =
        Uri.parse(match, uriQuote).run {
            parseResult {
                val z = Z_PATTERN.matchEntire(queryParams["z"]?.firstOrNull())?.doubleGroupOrNull()

                // Search or place with name
                // https://maps.apple.com/?q={name}
                // https://maps.apple.com/place?place-id={id}&q={name}
                val name = listOf(
                    "name",
                    "address",
                    "q",
                )
                    .firstNotNullOfOrNull { key ->
                        Q_PARAM_PATTERN.matchEntire(queryParams[key]?.firstOrNull())?.groupOrNull()
                    }

                // Directions
                // https://maps.apple.com/directions?source={point1Lat},{point1Lon}&waypoint={point2Lat},{point2Lon}&waypoint={point3Lat},{point4Lon}...&destination={lastPointLat},{lastPointLon}
                // https://maps.apple.com/directions?source={point1Name}&waypoint={point2Name}&waypoint={point3Name}...&destination={lastPointName}
                if (pathParts.getOrNull(1) == "directions") {
                    points = listOf(
                        "source",
                        "waypoint",
                        "destination",
                    )
                        .mapNotNull { key ->
                            queryParams[key]?.mapNotNull { value ->
                                LAT_LON_PATTERN.matchEntire(value)?.toLatLonPoint(Source.URI)
                                    ?: Q_PARAM_PATTERN.matchEntire(value)?.toNamePoint(Source.URI)
                            }
                        }
                        .flatten()
                        .map { WGS84Point(it, z = z) }
                        .toImmutableList()
                    return@parseResult
                }

                // API directions
                // https://maps.apple.com/?saddr={lat},{lon}&daddr={lat},{lon}
                // https://maps.apple.com/?saddr={name}&daddr={name}
                listOf(
                    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "saddr",
                    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "daddr",
                )
                    .mapNotNull { key ->
                        LAT_LON_PATTERN.matchEntire(queryParams[key]?.firstOrNull())?.toLatLonPoint(Source.URI)
                            ?: Q_PARAM_PATTERN.matchEntire(queryParams[key]?.firstOrNull())?.toNamePoint(Source.URI)
                    }
                    .takeIf { it.isNotEmpty() }
                    ?.let { naivePoints ->
                        points = naivePoints.map { WGS84Point(it, z) }.toImmutableList()
                        return@parseResult
                    }

                // Coordinates
                // https://maps.apple.com/?ll={lat},{lon}
                listOf(
                    "ll",
                    "coordinate",
                    "q",
                )
                    .firstNotNullOfOrNull { key ->
                        LAT_LON_PATTERN.matchEntire(queryParams[key]?.firstOrNull())?.toLatLonPoint(Source.URI)
                    }?.let {
                        points = persistentListOf(WGS84Point(it, z, name))
                        return@parseResult
                    }

                // Map center (including the search center 'sll')
                // https://maps.apple.com/?center={lat},{lon}
                listOf(
                    "sll",
                    "near",
                    "center",
                )
                    .firstNotNullOfOrNull { key ->
                        LAT_LON_PATTERN.matchEntire(queryParams[key]?.firstOrNull())?.toLatLonPoint(Source.MAP_CENTER)
                    }?.let {
                        points = persistentListOf(WGS84Point(it, z, name))
                        return@parseResult
                    }

                if (
                // Short link
                // https://maps.apple/p/{hash}
                    pathParts.getOrNull(1) == "p" ||
                    // Place with AUID
                    // https://maps.apple.com/place?auid={id}
                    !queryParams[@Suppress(
                        "GrazieInspectionRunner",
                        "SpellCheckingInspection"
                    ) "auid"].isNullOrEmpty() ||
                    // Place with place id
                    // https://maps.apple.com/place?place-id={id}
                    !queryParams["place-id"]?.firstOrNull().isNullOrEmpty()
                ) {
                    next = MatchedInput(appleMapsHtmlInput.get(), match)
                }

                if (name != null) {
                    points = persistentListOf(WGS84Point(z = z, name = name, source = Source.URI))
                }
            }
        }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(
            point,
            listOf(
                "https://maps.apple.com/?ll={lat}%2C{lon}&z={z}&q={name}",
                "https://maps.apple.com/?daddr={lat}%2C{lon}",
            ).random(),
        )

    override fun toString() = "AppleMapsUriInput"
}
