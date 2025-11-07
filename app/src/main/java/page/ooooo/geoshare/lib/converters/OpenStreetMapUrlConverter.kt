package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matches
import java.net.URL

class OpenStreetMapUrlConverter : UrlConverter.WithUriPattern, UrlConverter.WithHtmlPattern {
    companion object {
        const val ELEMENT_PATH = """/(?P<type>node|relation|way)/(?P<id>\d+)([/?#].*|$)"""
        const val HASH = """(?P<hash>[A-Za-z0-9_~]+-+)"""
    }

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?(openstreetmap|osm)\.org/\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_open_street_map_name,
        inputs = listOf(
            DocumentationInput.Url(20, "https://www.openstreetmap.org/"),
            DocumentationInput.Url(23, "https://www.openstreetmap.org/node"),
            DocumentationInput.Url(23, "https://www.openstreetmap.org/relation"),
            DocumentationInput.Url(23, "https://www.openstreetmap.org/way"),
            DocumentationInput.Url(23, "https://osm.org/"),
            DocumentationInput.Url(23, "https://osm.org/go/"),
        ),
    )

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        on { path matches """/go/$HASH""" } doReturn { OpenStreetMapGeoHashPositionMatch(it) }
        on { path matches ELEMENT_PATH } doReturn { PositionMatch(it) }
        on { fragment matches """map=$Z/$LAT/$LON.*""" } doReturn { PositionMatch(it) }
    }

    override val conversionHtmlPattern = conversionPattern<String, PositionMatch> {
        on { this find """"lat":$LAT,"lon":$LON""" } doReturn { PointsPositionMatch(it) }
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

    private class OpenStreetMapGeoHashPositionMatch(matcher: Matcher) : GeoHashPositionMatch(matcher) {
        override fun decode(hash: String) = decodeOpenStreetMapQuadTileHash(hash)
    }
}
