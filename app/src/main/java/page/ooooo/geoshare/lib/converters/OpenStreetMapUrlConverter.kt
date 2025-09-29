package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import java.net.MalformedURLException
import java.net.URL

class OpenStreetMapUrlConverter : UrlConverter.WithUriPattern, UrlConverter.WithHtmlPattern {
    companion object {
        const val ELEMENT_PATH = """/(?P<type>node|relation|way)/(?P<id>\d+)([/?#].*|$)"""
    }

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(www\.)?openstreetmap\.org/\S+""")

    override val conversionUriPattern = uriPattern {
        path(PositionRegex(ELEMENT_PATH))
        fragment(PositionRegex("""map=$Z/$LAT/$LON.*"""))
    }

    override val conversionHtmlPattern = htmlPattern<PositionRegex> {
        content(PointsPositionRegex(""""lat":$LAT,"lon":$LON"""))
    }

    @Throws(
        IllegalArgumentException::class,
        MalformedURLException::class,
    )
    override fun getHtmlUrl(uri: Uri): URL {
        val m = Pattern.compile(ELEMENT_PATH).matcher(uri.path)
        if (!m.matches()) {
            throw IllegalArgumentException("URI path does not match an OpenStreetMap element URI path")
        }
        val type = m.group("type")
        val id = m.group("id")
        return URL("https://www.openstreetmap.org/api/0.6/$type/$id${if (type != "node") "/full" else ""}.json")
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_open_street_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_open_street_map_loading_indicator_title
}
