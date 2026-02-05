package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.ImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toZLatLonPoint
import page.ooooo.geoshare.lib.geo.decodeOpenStreetMapQuadTileHash
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints

object OpenStreetMapInput : Input.HasHtml {
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

    override suspend fun parseUri(uri: Uri) = buildParseUriResult {
        points = buildPoints {
            uri.run {
                Regex("""/go/($HASH)""").matchEntire(path)
                    ?.groupOrNull()
                    ?.let { hash -> decodeOpenStreetMapQuadTileHash(hash) }
                    ?.let { (lat, lon, z) -> NaivePoint(lat, lon, z) }
                    ?.also { points.add(it) }
                    ?: Regex("""map=$Z/$LAT/$LON.*""").matchEntire(fragment)?.toZLatLonPoint()?.also { points.add(it) }
                    ?: LAT_LON_PATTERN.matchEntire(queryParams["to"])?.toLatLonPoint()?.also { points.add(it) }
                    ?: Regex(ELEMENT_PATH).matchEntire(path)?.let { m ->
                        m.groupOrNull(1)?.let { type ->
                            m.groupOrNull(2)?.let { id ->
                                htmlUriString =
                                    "https://www.openstreetmap.org/api/0.6/$type/$id${if (type != "node") "/full" else ""}.json"
                            }
                        }
                    }
            }
        }.asWGS84()
    }

    override suspend fun parseHtml(
        htmlUrlString: String,
        channel: ByteReadChannel,
        pointsFromUri: ImmutableList<Point>,
        log: ILog,
    ) = buildParseHtmlResult {
        points = buildPoints {
            defaultName = pointsFromUri.lastOrNull()?.name

            val pattern = Regex(""""lat":$LAT,"lon":$LON""")
            while (true) {
                val line = channel.readLine() ?: break
                points.addAll(pattern.findAll(line).mapNotNull { it.toLatLonPoint() })
            }
        }.asWGS84()
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_open_street_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_open_street_map_loading_indicator_title
}
