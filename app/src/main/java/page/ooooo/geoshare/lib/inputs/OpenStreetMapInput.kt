package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.conversion.ConversionPattern
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.LAT
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.LON
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.Z
import page.ooooo.geoshare.lib.decodeOpenStreetMapQuadTileHash
import page.ooooo.geoshare.lib.extensions.findAll
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.position.Srs
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

    override val conversionUriPattern = ConversionPattern.uriPattern(srs) {
        pointsAndZoomTriple {
            ("""/go/$HASH""" match path)?.groupOrNull("hash")?.let { decodeOpenStreetMapQuadTileHash(it) }
        }
        url {
            (ELEMENT_PATH match path)?.let { m ->
                m.groupOrNull("type")?.let { type ->
                    m.groupOrNull("id")?.let { id ->
                        URL("https://www.openstreetmap.org/api/0.6/$type/$id${if (type != "node") "/full" else ""}.json")
                    }
                }
            }
        }
        pointsAndZoom { """map=$Z/$LAT/$LON.*""" match fragment }
    }

    override val conversionHtmlPattern = ConversionPattern.htmlPattern(srs) {
        val pattern = Pattern.compile(""""lat":$LAT,"lon":$LON""")
        forEachLine {
            pointsSequence { pattern findAll this }
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_open_street_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_open_street_map_loading_indicator_title
}
