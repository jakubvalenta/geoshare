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
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.matches
import page.ooooo.geoshare.lib.extensions.toScale

/**
 * See https://developers.google.com/waze/deeplinks/
 */
class WazeUrlConverter : UrlConverter.WithUriPattern, UrlConverter.WithHtmlPattern {
    private val srs = Srs.WGS84

    companion object {
        @Suppress("SpellCheckingInspection")
        const val HASH = """(?P<hash>[0-9bcdefghjkmnpqrstuvwxyz]+)"""
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

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        all {
            optional {
                on { queryParams["z"]?.let { it matches Z } } doReturn { PositionMatch(it, srs) }
            }
            first {
                on { path matches """/ul/h$HASH""" } doReturn { WazeGeoHashPositionMatch(it, srs) }
                on { queryParams["h"]?.let { it matches HASH } } doReturn { WazeGeoHashPositionMatch(it, srs) }
                on { queryParams["to"]?.let { it matches """ll\.$LAT,$LON""" } } doReturn { PositionMatch(it, srs) }
                on { queryParams["ll"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it, srs) }
                @Suppress("SpellCheckingInspection")
                on { queryParams["latlng"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch(it, srs) }
                on { queryParams["q"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it, srs) }
                on { queryParams["venue_id"]?.let { it matches ".+" } } doReturn { PositionMatch(it, srs) }
                on { queryParams["place"]?.let { it matches ".+" } } doReturn { PositionMatch(it, srs) }
                on { queryParams["to"]?.let { it matches """place\..+""" } } doReturn { PositionMatch(it, srs) }
            }
        }
    }

    override val conversionHtmlPattern = conversionPattern<String, PositionMatch> {
        on { this find """"latLng":{"lat":$LAT,"lng":$LON}""" } doReturn { PositionMatch(it, srs) }
    }

    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_waze_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_waze_loading_indicator_title

    private class WazeGeoHashPositionMatch(matcher: Matcher, srs: Srs) : GeoHashPositionMatch(matcher, srs) {
        override fun decode(hash: String) = decodeWazeGeoHash(hash)
            .let { (lat, lon, z) -> Triple(lat.toScale(6), lon.toScale(6), z) }
    }
}
