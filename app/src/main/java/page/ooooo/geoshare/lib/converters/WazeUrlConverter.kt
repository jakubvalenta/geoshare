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
                onUri { queryParams["z"]?.let { it matcherIfMatches Z } } doReturn { PositionMatch(it) }
            }
            first {
                onUri { path matcherIfMatches """/ul/h$HASH""" } doReturn { WazeGeoHashPositionMatch(it) }
                onUri { queryParams["h"]?.let { it matcherIfMatches HASH } } doReturn { WazeGeoHashPositionMatch(it) }
                onUri { queryParams["to"]?.let { it matcherIfMatches """ll\.$LAT,$LON""" } } doReturn { PositionMatch(it) }
                onUri { queryParams["ll"]?.let { it matcherIfMatches "$LAT,$LON" } } doReturn { PositionMatch(it) }
                @Suppress("SpellCheckingInspection")
                onUri { queryParams["latlng"]?.let { it matcherIfMatches "$LAT,$LON" } } doReturn { PositionMatch(it) }
                onUri { queryParams["q"]?.let { it matcherIfMatches Q_PARAM } } doReturn { PositionMatch(it) }
                onUri { queryParams["venue_id"]?.let { it matcherIfMatches ".+" } } doReturn { PositionMatch(it) }
                onUri { queryParams["place"]?.let { it matcherIfMatches ".+" } } doReturn { PositionMatch(it) }
                onUri { queryParams["to"]?.let { it matcherIfMatches """place\..+""" } } doReturn { PositionMatch(it) }
            }
        }
    }

    override val conversionHtmlPattern = conversionPattern {
        onHtml { this matcherIfFind """"latLng":{"lat":$LAT,"lng":$LON}""" } doReturn { PositionMatch(it) }
    }

    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_waze_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_waze_loading_indicator_title

    private class WazeGeoHashPositionMatch(matcher: Matcher) : GeoHashPositionMatch(matcher) {
        override fun decode(hash: String) = decodeWazeGeoHash(hash)
            .let { (lat, lon, z) -> Triple(lat.toScale(6), lon.toScale(6), z) }
    }
}
