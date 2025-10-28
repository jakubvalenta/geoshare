package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Matcher
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
        const val HASH = """(?P<hash>[0-9bcdefghjkmnpqrstuvwxyz]+)"""

        @Suppress("SpellCheckingInspection")
        val HASH_CHAR_MAP = "0123456789bcdefghjkmnpqrstuvwxyz".mapIndexed { i, char -> char to i }.toMap()

        /**
         * See https://en.wikipedia.org/wiki/Geohash#Algorithm_and_example
         */
        fun decodeGeoHash(hash: String): Triple<Double, Double, Int> =
            decodeGeoHash(hash, HASH_CHAR_MAP, 5, useMeanValue = true)
    }

    class WazeGeoHashPositionMatch(matcher: Matcher) : GeoHashPositionMatch(matcher) {
        override fun decode(hash: String) = decodeGeoHash(hash).let { (lat, lon, z) ->
            Triple(lat.toScale(6), lon.toScale(6), z)
        }
    }

    class WazeGeoHashPositionRegex(regex: String) : PositionRegex(regex) {
        override fun matches(input: String) = pattern.matcherIfMatches(input)?.let { WazeGeoHashPositionMatch(it) }
        override fun find(input: String) = pattern.matcherIfFind(input)?.let { WazeGeoHashPositionMatch(it) }
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

    override val conversionUriPattern = uriPattern {
        all {
            optional {
                query("z", PositionRegex(Z))
            }
            first {
                path(WazeGeoHashPositionRegex("""/ul/h$HASH"""))
                query("h", WazeGeoHashPositionRegex(HASH))
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

    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_waze_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_waze_loading_indicator_title
}
