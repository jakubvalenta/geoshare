package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toZLatLonPoint
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.geo.decodeOpenStreetMapQuadTileHash
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenStreetMapUriInput @Inject constructor(
    private val openStreetMapApiInput: dagger.Lazy<OpenStreetMapApiInput>,
    val uriQuote: UriQuote,
) : BasicOfflineInput, Input.HasPattern, Input.HasRandomUri {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.OPEN_STREET_MAP
    override val changelog = persistentListOf(
        InputChangelogItem.Url(20, "https://www.openstreetmap.org/"),
        InputChangelogItem.Url(31, "https://www.openstreetmap.org/directions"),
        InputChangelogItem.Url(23, "https://www.openstreetmap.org/node"),
        InputChangelogItem.Url(23, "https://www.openstreetmap.org/relation"),
        InputChangelogItem.Url(23, "https://www.openstreetmap.org/way"),
        InputChangelogItem.Url(23, "https://osm.org/"),
        InputChangelogItem.Url(23, "https://osm.org/go/"),
    )

    override val pattern = Regex("""((?:https?://)?(?:www\.)?(?:openstreetmap|osm)\.org/$URI_REST)""")

    override fun parse(match: String, resources: Resources) =
        Uri.parse(match, uriQuote).run {
            parseResult {
                // Short link
                // https://osm.org/go/{hash}
                if (pathParts.firstOrNull() == "" && pathParts.getOrNull(1) == "go") {
                    Regex(HASH).matchEntire(pathParts.getOrNull(2))?.value
                        ?.let { hash -> decodeOpenStreetMapQuadTileHash(hash) }
                        ?.let {
                            points = persistentListOf(WGS84Point(it))
                            return@parseResult
                        }
                }

                // Map center
                // https://www.openstreetmap.org/#map={z}/{lat}/{lon}
                Regex("""map=$Z/$LAT/$LON.*""").matchEntire(fragment)?.toZLatLonPoint(Source.MAP_CENTER)?.let {
                    points = persistentListOf(WGS84Point(it))
                    return@parseResult
                }

                // Coordinates
                // https://www.openstreetmap.org/?lat={lat}&lon={lon}&zoom={z}
                // Pin
                // https://www.openstreetmap.org/?mlat={lat}&mlon={lon}&zoom={z}
                listOf(@Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "mlat", "lat")
                    .firstNotNullOfOrNull { key ->
                        LAT_PATTERN.matchEntire(queryParams[key]?.firstOrNull())?.doubleGroupOrNull()
                    }
                    ?.let { lat ->
                        listOf(@Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "mlon", "lon")
                            .firstNotNullOfOrNull { key ->
                                LON_PATTERN.matchEntire(queryParams[key]?.firstOrNull())?.doubleGroupOrNull()
                            }
                            ?.let { lon ->
                                val z = listOf("z", "zoom")
                                    .firstNotNullOfOrNull { key ->
                                        Z_PATTERN.matchEntire(queryParams[key]?.firstOrNull())?.doubleGroupOrNull()
                                    }
                                points = persistentListOf(WGS84Point(lat, lon, z, source = Source.URI))
                                return@parseResult
                            }
                    }

                // Directions
                // https://www.openstreetmap.org/directions?to={lat},{lon}
                LAT_LON_PATTERN.matchEntire(queryParams["to"]?.firstOrNull())?.toLatLonPoint(Source.URI)?.let {
                    points = persistentListOf(WGS84Point(it))
                    return@parseResult
                }

                when (pathParts.getOrNull(1)) {
                    "node" ->
                        // Node
                        // https://www.openstreetmap.org/node/{id}
                        pathParts.getOrNull(2)?.toLongOrNull()?.let { osmId ->
                            next = MatchedInput(
                                openStreetMapApiInput.get(),
                                OpenStreetMapApiInput.formatUrlString(
                                    OpenStreetMapApiInput.Companion.ElementType.NODE, osmId
                                ),
                            )
                            return@parseResult
                        }

                    "relation" ->
                        // Relation
                        // https://www.openstreetmap.org/relation/{id}
                        pathParts.getOrNull(2)?.toLongOrNull()?.let { osmId ->
                            next = MatchedInput(
                                openStreetMapApiInput.get(),
                                OpenStreetMapApiInput.formatUrlString(
                                    OpenStreetMapApiInput.Companion.ElementType.RELATION, osmId
                                ),
                            )
                            return@parseResult
                        }

                    "way" ->
                        // Way
                        // https://www.openstreetmap.org/way/{id}
                        pathParts.getOrNull(2)?.toLongOrNull()?.let { osmId ->
                            next = MatchedInput(
                                openStreetMapApiInput.get(),
                                OpenStreetMapApiInput.formatUrlString(
                                    OpenStreetMapApiInput.Companion.ElementType.WAY, osmId
                                ),
                            )
                            return@parseResult
                        }
                }
            }
        }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "https://www.openstreetmap.org/#map={z}/{lat}/{lon}")

    override fun toString() = "OpenStreetMapUriInput"

    private companion object {
        private const val HASH = """[A-Za-z0-9_~]+-+"""
    }
}
