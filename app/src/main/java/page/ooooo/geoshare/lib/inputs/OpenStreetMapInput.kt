package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlinx.io.Source
import kotlinx.io.readLine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.extensions.findAll
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matches
import page.ooooo.geoshare.lib.position.Srs
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

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        on { path matches """/go/$HASH""" } doReturn { OpenStreetMapGeoHashPositionMatch(it, srs) }
        on { path matches ELEMENT_PATH } doReturn { PositionMatch.Empty(it, srs) }
        on { fragment matches """map=$Z/$LAT/$LON.*""" } doReturn { PositionMatch.LatLonZ(it, srs) }
    }

    override val conversionHtmlPattern = conversionPattern<Source, PositionMatch> {
        onEach {
            sequence {
                for (line in generateSequence { this@onEach.readLine() }) {
                    yieldAll(line findAll """"lat":$LAT,"lon":$LON""")
                }
            }
        } doReturn { PositionMatch.LatLon(it, srs) }
    }

    override val conversionHtmlRedirectPattern = null

    override fun getHtmlUrl(uri: Uri): URL? {
        val m = (uri.path matches ELEMENT_PATH) ?: return null
        val type = m.groupOrNull("type") ?: return null
        val id = m.groupOrNull("id") ?: return null
        return URL("https://www.openstreetmap.org/api/0.6/$type/$id${if (type != "node") "/full" else ""}.json")
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_open_street_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_open_street_map_loading_indicator_title

    private class OpenStreetMapGeoHashPositionMatch(matcher: Matcher, srs: Srs) : PositionMatch.GeoHash(matcher, srs) {
        override fun decode(hash: String) = decodeOpenStreetMapQuadTileHash(hash)
    }
}
