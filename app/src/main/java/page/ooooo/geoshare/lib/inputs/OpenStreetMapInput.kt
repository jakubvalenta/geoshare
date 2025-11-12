package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import kotlinx.io.Source
import kotlinx.io.readLine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.conversion.ConversionPattern
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.LAT
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.LON
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.Z
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.decodeOpenStreetMapQuadTileHash
import page.ooooo.geoshare.lib.extensions.findAll
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.position.toLatLon
import page.ooooo.geoshare.lib.position.toLatLonZ
import java.net.URL

object OpenStreetMapInput : Input.HasUri, Input.HasHtml {
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

    override val conversionUriPattern = ConversionPattern.first<Uri, Position> {
        pattern {
            ("""/go/$HASH""" match path)?.groupOrNull("hash")?.let { hash ->
                decodeOpenStreetMapQuadTileHash(hash).let { (lat, lon, z) ->
                    Position(srs, lat, lon, z = z)
                }
            }
        }
        pattern { (ELEMENT_PATH match path)?.let { Position(srs) } }
        pattern { ("""map=$Z/$LAT/$LON.*""" match fragment)?.toLatLonZ(srs) }
    }

    override val conversionHtmlPattern = ConversionPattern.first<Source, Position> {
        listPattern {
            val pattern = Pattern.compile(""""lat":$LAT,"lon":$LON""")
            generateSequence { this@listPattern.readLine() }
                .flatMap { line -> pattern findAll line }
                .mapNotNull { m -> m.toLatLon(srs) }
                .toList()
        }
    }

    override val conversionHtmlRedirectPattern = null

    override fun getHtmlUrl(uri: Uri): URL? {
        val m = (ELEMENT_PATH match uri.path) ?: return null
        val type = m.groupOrNull("type") ?: return null
        val id = m.groupOrNull("id") ?: return null
        return URL("https://www.openstreetmap.org/api/0.6/$type/$id${if (type != "node") "/full" else ""}.json")
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_open_street_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_open_street_map_loading_indicator_title
}
