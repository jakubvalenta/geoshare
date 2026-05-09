package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toZLatLonPoint
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.geo.decodeOpenStreetMapQuadTileHash

object OpenStreetMapUriInput : UriInput, Input.HasRandomUri {
    private const val ELEMENT_PATH = """/(node|relation|way)/(\d+)(?:[/?#].*|$)"""
    private const val HASH = """[A-Za-z0-9_~]+-+"""

    override val pattern = Regex("""((?:https?://)?(?:www\.)?(?:openstreetmap|osm)\.org/$URI_REST)""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.OPEN_STREET_MAP,
        nameResId = R.string.converter_open_street_map_name,
        items = listOf(
            InputDocumentationItem.Url(20, "https://www.openstreetmap.org/"),
            InputDocumentationItem.Url(31, "https://www.openstreetmap.org/directions"),
            InputDocumentationItem.Url(23, "https://www.openstreetmap.org/node"),
            InputDocumentationItem.Url(23, "https://www.openstreetmap.org/relation"),
            InputDocumentationItem.Url(23, "https://www.openstreetmap.org/way"),
            InputDocumentationItem.Url(23, "https://osm.org/"),
            InputDocumentationItem.Url(23, "https://osm.org/go/"),
        ),
    )

    override suspend fun parse(
        data: Uri,
        match: String,
        prevPoints: Points?,
        uriQuote: UriQuote,
        log: ILog,
    ) = buildParseResult {
        data.run {
            // Short link
            // https://osm.org/go/{hash}
            Regex("""/go/($HASH)""").matchEntire(path)?.groupOrNull()
                ?.let { hash -> decodeOpenStreetMapQuadTileHash(hash) }
                ?.let {
                    points = persistentListOf(WGS84Point(it))
                    return@run
                }

            // Map center
            // https://www.openstreetmap.org/#map={z}/{lat}/{lon}
            Regex("""map=$Z/$LAT/$LON.*""").matchEntire(fragment)?.toZLatLonPoint(Source.MAP_CENTER)?.let {
                points = persistentListOf(WGS84Point(it))
                return@run
            }

            // Coordinates
            // https://www.openstreetmap.org/?lat={lat}&lon={lon}&zoom={z}
            LAT_PATTERN.matchEntire(queryParams["lat"])?.doubleGroupOrNull()?.let { lat ->
                LON_PATTERN.matchEntire(queryParams["lon"])?.doubleGroupOrNull()?.let { lon ->
                    val z = listOf("z", "zoom")
                        .firstNotNullOfOrNull { key -> Z_PATTERN.matchEntire(queryParams[key])?.doubleGroupOrNull() }
                    points = persistentListOf(WGS84Point(lat, lon, z, source = Source.URI))
                    return@run
                }
            }

            // Directions
            // https://www.openstreetmap.org/directions?to={lat},{lon}
            LAT_LON_PATTERN.matchEntire(queryParams["to"])?.toLatLonPoint(Source.URI)?.let {
                points = persistentListOf(WGS84Point(it))
                return@run
            }

            // Element
            // https://www.openstreetmap.org/node/{id}
            // https://www.openstreetmap.org/relation/{id}
            // https://www.openstreetmap.org/way/{id}
            Regex(ELEMENT_PATH).matchEntire(path)?.let { m ->
                m.groupOrNull(1)?.let { type ->
                    m.groupOrNull(2)?.let { id ->
                        nextMatch =
                            "https://www.openstreetmap.org/api/0.6/$type/$id${if (type != "node") "/full" else ""}.json"
                        nextInput = OpenStreetMapApiInput
                    }
                }
            }
        }
    }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "https://www.openstreetmap.org/#map={z}/{lat}/{lon}")
}
