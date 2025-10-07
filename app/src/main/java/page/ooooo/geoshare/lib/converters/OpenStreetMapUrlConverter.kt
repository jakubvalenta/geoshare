package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import java.net.URL

class OpenStreetMapUrlConverter : UrlConverter.WithUriPattern, UrlConverter.WithHtmlPattern {
    companion object {
        const val ELEMENT_PATH = """/(?P<type>node|relation|way)/(?P<id>\d+)([/?#].*|$)"""
        const val MODIFIED_BASE64_REGEX = """(?P<hash>[A-Za-z0-9_~]+)(?P<relativeZoom>-*)"""

        @Suppress("SpellCheckingInspection")
        const val MODIFIED_BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_~"
        const val MODIFIED_BASE64_BIT_COUNT = 6
        const val MODIFIED_BASE64_ZOOM_CHAR = '-'
        val modifiedBase64Map = MODIFIED_BASE64_CHARS.mapIndexed { i, char -> char to i }.toMap()
    }

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?(openstreetmap|osm)\.org/\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_open_street_map_name,
        inputs = listOf(
            DocumentationInput.Url("https://www.openstreetmap.org/", 20),
            DocumentationInput.Url("https://www.openstreetmap.org/node", 23),
            DocumentationInput.Url("https://www.openstreetmap.org/relation", 23),
            DocumentationInput.Url("https://www.openstreetmap.org/way", 23),
            DocumentationInput.Url("https://osm.org/", 23),
            DocumentationInput.Url("https://osm.org/go/", 23),
        ),
    )

    override val conversionUriPattern = uriPattern {
        path(
            GeoHashPositionRegex(
                """/go/$MODIFIED_BASE64_REGEX""",
                modifiedBase64Map,
                MODIFIED_BASE64_BIT_COUNT,
                MODIFIED_BASE64_ZOOM_CHAR,
            )
        )
        path(PositionRegex(ELEMENT_PATH))
        fragment(PositionRegex("""map=$Z/$LAT/$LON.*"""))
    }

    override val conversionHtmlPattern = htmlPattern<PositionRegex> {
        content(PointsPositionRegex(""""lat":$LAT,"lon":$LON"""))
    }

    override fun getHtmlUrl(uri: Uri): URL? {
        val m = Pattern.compile(ELEMENT_PATH).matcher(uri.path)
        if (!m.matches()) {
            return null
        }
        val type = try {
            m.group("type")
        } catch (_: IllegalArgumentException) {
            return null
        }
        val id = try {
            m.group("id")
        } catch (_: IllegalArgumentException) {
            return null
        }
        return URL("https://www.openstreetmap.org/api/0.6/$type/$id${if (type != "node") "/full" else ""}.json")
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_open_street_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_open_street_map_loading_indicator_title
}
