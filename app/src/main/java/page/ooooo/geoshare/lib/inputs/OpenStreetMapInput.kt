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
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toZLatLonPoint
import page.ooooo.geoshare.lib.geo.decodeOpenStreetMapQuadTileHash
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.WGS84Point

object OpenStreetMapInput : HtmlInput, Input.HasRandomUri {
    private const val ELEMENT_PATH = """/(node|relation|way)/(\d+)(?:[/?#].*|$)"""
    private const val HASH = """[A-Za-z0-9_~]+-+"""

    override val uriPattern = Regex("""(?:https?://)?(?:www\.)?(?:openstreetmap|osm)\.org/\S+""")
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

    override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = buildParseUriResult {
        uri.run {
            // Short link
            // https://osm.org/go/{hash}
            Regex("""/go/($HASH)""").matchEntire(path)?.groupOrNull()
                ?.let { hash -> decodeOpenStreetMapQuadTileHash(hash) }
                ?.let {
                    points = persistentListOf(it.asWGS84())
                    return@run
                }

            // Coordinates
            // https://www.openstreetmap.org/#map={z}/{lat}/{lon}
            Regex("""map=$Z/$LAT/$LON.*""").matchEntire(fragment)?.toZLatLonPoint()?.let {
                points = persistentListOf(it.asWGS84())
                return@run
            }

            // Directions
            // https://www.openstreetmap.org/directions?to={lat},{lon}
            LAT_LON_PATTERN.matchEntire(queryParams["to"])?.toLatLonPoint()?.let {
                points = persistentListOf(it.asWGS84())
                return@run
            }

            // Element
            // https://www.openstreetmap.org/node/{id}
            // https://www.openstreetmap.org/relation/{id}
            // https://www.openstreetmap.org/way/{id}
            Regex(ELEMENT_PATH).matchEntire(path)?.let { m ->
                m.groupOrNull(1)?.let { type ->
                    m.groupOrNull(2)?.let { id ->
                        htmlUriString =
                            "https://www.openstreetmap.org/api/0.6/$type/$id${if (type != "node") "/full" else ""}.json"
                    }
                }
            }
        }
    }

    override suspend fun parseHtml(
        htmlUrlString: String,
        channel: ByteReadChannel,
        pointsFromUri: ImmutableList<Point>,
        uriQuote: UriQuote,
        log: ILog,
    ) = buildParseHtmlResult {
        val name = pointsFromUri.lastOrNull()?.name
        val mutablePoints = mutableListOf<WGS84Point>()

        val pattern = Regex(""""lat":$LAT,"lon":$LON""")
        while (true) {
            val line = channel.readLine() ?: break
            mutablePoints.addAll(pattern.findAll(line).mapNotNull { it.toLatLonPoint()?.asWGS84() })
        }

        if (name != null) {
            mutablePoints.removeLastOrNull()?.let { lastPoint ->
                mutablePoints.add(lastPoint.copy(name = name))
            }
        }

        points = mutablePoints.toImmutableList()
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_open_street_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_open_street_map_loading_indicator_title

    override fun genRandomUri(point: Point) =
        point.formatUriString("https://www.openstreetmap.org/#map={z}/{lat}/{lon}")
}
