package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.collections.immutable.ImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.findAllNaivePoint
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.extensions.matchHash
import page.ooooo.geoshare.lib.extensions.matchNaivePoint
import page.ooooo.geoshare.lib.geo.decodeOpenStreetMapQuadTileHash
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints
import page.ooooo.geoshare.lib.point.toParseHtmlResult
import page.ooooo.geoshare.lib.point.toParseUriResult

object OpenStreetMapInput : Input.HasHtml {
    private const val ELEMENT_PATH = """/(?P<type>node|relation|way)/(?P<id>\d+)([/?#].*|$)"""
    private const val HASH = """(?P<hash>[A-Za-z0-9_~]+-+)"""

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?(openstreetmap|osm)\.org/\S+""")
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

    override suspend fun parseUri(uri: Uri): ParseUriResult? {
        var htmlUriString: String? = null
        return buildPoints {
            uri.run {
                ("""/go/$HASH""" matchHash path)
                    ?.let { hash -> decodeOpenStreetMapQuadTileHash(hash) }
                    ?.let { (lat, lon, z) -> NaivePoint(lat, lon, z) }
                    ?.also { points.add(it) }
                    ?: ("""map=$Z/$LAT/$LON.*""" matchNaivePoint fragment)?.also { points.add(it) }
                    ?: (LAT_LON_PATTERN matchNaivePoint queryParams["to"])?.also { points.add(it) }
                    ?: (ELEMENT_PATH match path)?.let { m ->
                        m.groupOrNull("type")?.let { type ->
                            m.groupOrNull("id")?.let { id ->
                                htmlUriString =
                                    "https://www.openstreetmap.org/api/0.6/$type/$id${if (type != "node") "/full" else ""}.json"
                            }
                        }
                    }
            }
        }
            .asWGS84()
            .toParseUriResult(htmlUriString)
    }

    override suspend fun parseHtml(
        channel: ByteReadChannel,
        pointsFromUri: ImmutableList<Point>,
        log: ILog,
    ): ParseHtmlResult? =
        buildPoints {
            defaultName = pointsFromUri.lastOrNull()?.name

            val pattern = Pattern.compile(""""lat":$LAT,"lon":$LON""")
            while (true) {
                val line = channel.readUTF8Line() ?: break
                points.addAll(pattern findAllNaivePoint line)
            }
        }
            .asWGS84()
            .toParseHtmlResult()

    @StringRes
    override val permissionTitleResId = R.string.converter_open_street_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_open_street_map_loading_indicator_title
}
