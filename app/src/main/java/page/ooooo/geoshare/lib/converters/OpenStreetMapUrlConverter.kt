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
    }

    @StringRes
    override val nameResId = R.string.converter_open_street_map_name

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?openstreetmap\.org/\S+""")
    override val supportedInputs = listOf(
        SupportedInput.Url("https://www.openstreetmap.org/", 20),
        SupportedInput.Url("https://www.openstreetmap.org/node", 23),
        SupportedInput.Url("https://www.openstreetmap.org/relation", 23),
        SupportedInput.Url("https://www.openstreetmap.org/way", 23),
    )

    override val conversionUriPattern = uriPattern {
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
