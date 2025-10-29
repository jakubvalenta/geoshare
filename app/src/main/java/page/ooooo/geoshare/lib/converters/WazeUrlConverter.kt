package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z

/**
 * See https://developers.google.com/waze/deeplinks/
 */
class WazeUrlConverter : UrlConverter.WithUriPattern, UrlConverter.WithHtmlPattern {
    companion object {
        @Suppress("SpellCheckingInspection")
        const val HASH = """(?P<hash>[0-9bcdefghjkmnpqrstuvwxyz]+)"""

        @Suppress("SpellCheckingInspection")
        private val HASH_CHAR_MAP = "0123456789bcdefghjkmnpqrstuvwxyz".mapIndexed { i, char -> char to i }.toMap()

        /**
         * See https://en.wikipedia.org/wiki/Geohash#Algorithm_and_example
         */
        fun decodeGeoHash(hash: String): Triple<Double, Double, Int> =
            decodeGeoHash(hash, HASH_CHAR_MAP, 5, useMeanValue = true)
    }

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?((www|ul)\.)?waze\.com/\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_waze_name,
        inputs = listOf(
            DocumentationInput.Url(21, "https://waze.com/live-map"),
            DocumentationInput.Url(21, "https://waze.com/ul"),
            DocumentationInput.Url(21, "https://www.waze.com/live-map"),
            DocumentationInput.Url(21, "https://www.waze.com/ul"),
            DocumentationInput.Url(21, "https://ul.waze.com/ul"),
        ),
    )

    override val conversionUriPattern = conversionPattern {
        all {
            optional {
                query("z", Z) { PositionMatch(it) }
            }
            first {
                path("""/ul/h$HASH""") { WazeGeoHashPositionMatch(it) }
                query("h", HASH) { WazeGeoHashPositionMatch(it) }
                query("to", """ll\.$LAT,$LON""") { PositionMatch(it) }
                query("ll", "$LAT,$LON") { PositionMatch(it) }
                @Suppress("SpellCheckingInspection")
                query("latlng", "$LAT,$LON") { PositionMatch(it) }
                query("q", Q_PARAM) { PositionMatch(it) }
                query("venue_id", ".+") { PositionMatch(it) }
                query("place", ".+") { PositionMatch(it) }
                query("to", """place\..+""") { PositionMatch(it) }
            }
        }
    }

    override val conversionHtmlPattern = conversionPattern {
        html(""""latLng":{"lat":$LAT,"lng":$LON}""") { PositionMatch(it) }
    }

    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_waze_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_waze_loading_indicator_title

    private class WazeGeoHashPositionMatch(matcher: Matcher) : GeoHashPositionMatch(matcher) {
        override fun decode(hash: String) = decodeGeoHash(hash).let { (lat, lon, z) ->
            Triple(lat.toScale(6), lon.toScale(6), z)
        }
    }
}
