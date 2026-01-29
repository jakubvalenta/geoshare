package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import io.ktor.utils.io.*
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.*
import page.ooooo.geoshare.lib.geo.decodeOpenStreetMapQuadTileHash
import page.ooooo.geoshare.lib.position.*

object OpenStreetMapInput : Input.HasHtml {
    private const val ELEMENT_PATH = """/(?P<type>node|relation|way)/(?P<id>\d+)([/?#].*|$)"""
    private const val HASH = """(?P<hash>[A-Za-z0-9_~]+-+)"""

    private val srs = Srs.WGS84

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
        val position = buildPosition(srs) {
            uri.run {
                setPointIfNull {
                    ("""/go/$HASH""" matchHash path)
                        ?.let { hash -> decodeOpenStreetMapQuadTileHash(hash) }
                        ?.let { (lat, lon, z) -> LatLonZName(lat, lon, z) }
                }
                setPointIfNull { """map=$Z/$LAT/$LON.*""" matchLatLonZName fragment }
                setPointIfNull { LAT_LON_PATTERN matchLatLonZName queryParams["to"] }
                if (!hasPoint()) {
                    (ELEMENT_PATH match path)?.let { m ->
                        m.groupOrNull("type")?.let { type ->
                            m.groupOrNull("id")?.let { id ->
                                htmlUriString =
                                    "https://www.openstreetmap.org/api/0.6/$type/$id${if (type != "node") "/full" else ""}.json"
                            }
                        }
                    }
                }
            }
        }
        return ParseUriResult.from(position, htmlUriString)
    }

    override suspend fun parseHtml(channel: ByteReadChannel, positionFromUri: Position, log: ILog): ParseHtmlResult? {
        val positionFromHtml = buildPosition(srs) {
            val pattern = Pattern.compile(""""lat":$LAT,"lon":$LON""")
            while (true) {
                val line = channel.readUTF8Line() ?: break
                addPoints { pattern findAllLatLonZName line }
            }
        }
        return ParseHtmlResult.from(positionFromUri, positionFromHtml)
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_open_street_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_open_street_map_loading_indicator_title
}
