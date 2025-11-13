package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import kotlinx.io.Source
import kotlinx.io.readLine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.decodeOpenStreetMapQuadTileHash
import page.ooooo.geoshare.lib.extensions.findAll
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.position.*
import java.net.URL

object OpenStreetMapInput : Input.HasHtml {
    private const val ELEMENT_PATH = """/(?P<type>node|relation|way)/(?P<id>\d+)([/?#].*|$)"""
    private const val HASH = """(?P<hash>[A-Za-z0-9_~]+-+)"""

    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?(openstreetmap|osm)\.org/\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_open_street_map_name,
        inputs = listOf(
            Input.DocumentationInput.Url(20, "https://www.openstreetmap.org/"),
            Input.DocumentationInput.Url(23, "https://www.openstreetmap.org/node"),
            Input.DocumentationInput.Url(23, "https://www.openstreetmap.org/relation"),
            Input.DocumentationInput.Url(23, "https://www.openstreetmap.org/way"),
            Input.DocumentationInput.Url(23, "https://osm.org/"),
            Input.DocumentationInput.Url(23, "https://osm.org/go/"),
        ),
    )

    override fun parseUri(uri: Uri) = uri.run {
        PositionBuilder(srs).apply {
            setLatLonZoom {
                ("""/go/$HASH""" match path)?.groupOrNull("hash")?.let { hash ->
                    decodeOpenStreetMapQuadTileHash(hash)
                }
            }
            setPointAndZoomFromMatcher { """map=$Z/$LAT/$LON.*""" match fragment }
            setUrl {
                (ELEMENT_PATH match path)?.let { m ->
                    m.groupOrNull("type")?.let { type ->
                        m.groupOrNull("id")?.let { id ->
                            URL("https://www.openstreetmap.org/api/0.6/$type/$id${if (type != "node") "/full" else ""}.json")
                        }
                    }
                }
            }
        }
    }

    override fun parseHtml(source: Source) = source.run {
        PositionBuilder(srs).apply {
            val pattern = Pattern.compile(""""lat":$LAT,"lon":$LON""")
            for (line in generateSequence { source.readLine() }) {
                addPointsFromSequenceOfMatchers { pattern findAll line }
            }
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_open_street_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_open_street_map_loading_indicator_title
}
