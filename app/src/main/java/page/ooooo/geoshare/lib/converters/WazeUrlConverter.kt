package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z

/**
 * See https://developers.google.com/waze/deeplinks/
 */
class WazeUrlConverter : UrlConverter.WithUriPattern, UrlConverter.WithHtmlPattern {
    companion object {
        @Suppress("SpellCheckingInspection")
        const val BASE32 = """(?P<hash>[0-9bcdefghjkmnpqrstuvwxyz]+)"""
    }

    @StringRes
    override val nameResId = R.string.converter_waze_name

    class GeoHashRegex(regex: String) : PositionRegex(regex) {
        override val points: List<Point>?
            get() = groupOrNull("hash")?.let { hash ->
                decodeGeoHash(hash).let { (lat, lon) ->
                    listOf(lat.toString() to lon.toString())
                }
            }
    }

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?((www|ul)\.)?waze\.com/\S+""")
    override val supportedUriStrings = listOf(
        "https://waze.com/live-map",
        "https://waze.com/ul",
        "https://www.waze.com/live-map",
        "https://www.waze.com/ul",
        "https://ul.waze.com/ul",
    )

    override val conversionUriPattern = uriPattern {
        all {
            optional {
                query("z", PositionRegex(Z))
            }
            first {
                path(GeoHashRegex("""/ul/h$BASE32"""))
                query("h", GeoHashRegex(BASE32))
                query("to", PositionRegex("""ll\.$LAT,$LON"""))
                query("ll", PositionRegex("$LAT,$LON"))
                @Suppress("SpellCheckingInspection") query("latlng", PositionRegex("$LAT,$LON"))
                query("q", PositionRegex(Q_PARAM))
                query("venue_id", PositionRegex(".+"))
                query("place", PositionRegex(".+"))
                query("to", PositionRegex("""place\..+"""))
            }
        }
    }

    override val conversionHtmlPattern = htmlPattern {
        content(PositionRegex(""""latLng":{"lat":$LAT,"lng":$LON}"""))
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_waze_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_waze_loading_indicator_title
}
