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
import page.ooooo.geoshare.lib.PositionMatch.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.matches
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.position.Srs

/**
 * See https://developers.google.com/waze/deeplinks/
 */
object WazeInput : Input.HasUri, Input.HasHtml {
    @Suppress("SpellCheckingInspection")
    private const val HASH = """(?P<hash>[0-9bcdefghjkmnpqrstuvwxyz]+)"""

    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?((www|ul)\.)?waze\.com/\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_waze_name,
        inputs = listOf(
            Input.DocumentationInput.Url(21, "https://waze.com/live-map"),
            Input.DocumentationInput.Url(21, "https://waze.com/ul"),
            Input.DocumentationInput.Url(21, "https://www.waze.com/live-map"),
            Input.DocumentationInput.Url(21, "https://www.waze.com/ul"),
            Input.DocumentationInput.Url(21, "https://ul.waze.com/ul"),
        ),
    )

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        all {
            optional {
                on { queryParams["z"]?.let { it matches Z } } doReturn { PositionMatch.Zoom(it, srs) }
            }
            first {
                on { path matches """/ul/h$HASH""" } doReturn { WazeGeoHashPositionMatch(it, srs) }
                on { queryParams["h"]?.let { it matches HASH } } doReturn { WazeGeoHashPositionMatch(it, srs) }
                on { queryParams["to"]?.let { it matches """ll\.$LAT,$LON""" } } doReturn {
                    PositionMatch.LatLon(
                        it,
                        srs
                    )
                }
                on { queryParams["ll"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch.LatLon(it, srs) }
                @Suppress("SpellCheckingInspection")
                on { queryParams["latlng"]?.let { it matches "$LAT,$LON" } } doReturn { PositionMatch.LatLon(it, srs) }
                on { queryParams["q"]?.let { it matches Q_PARAM } } doReturn { PositionMatch.Query(it, srs) }
                on { queryParams["venue_id"]?.let { it matches ".+" } } doReturn { PositionMatch.Empty(it, srs) }
                on { queryParams["place"]?.let { it matches ".+" } } doReturn { PositionMatch.Empty(it, srs) }
                on { queryParams["to"]?.let { it matches """place\..+""" } } doReturn { PositionMatch.Empty(it, srs) }
            }
        }
    }

    override val conversionHtmlPattern = conversionPattern<Source, PositionMatch> {
        on {
            val pattern = Pattern.compile(""""latLng":{"lat":$LAT,"lng":$LON}""")
            generateSequence { this.readLine() }.firstNotNullOfOrNull { line ->
                line find pattern
            }
        } doReturn { PositionMatch.LatLon(it, srs) }
    }

    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_waze_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_waze_loading_indicator_title

    private class WazeGeoHashPositionMatch(matcher: Matcher, srs: Srs) : PositionMatch.GeoHash(matcher, srs) {
        override fun decode(hash: String) = decodeWazeGeoHash(hash).let { (lat, lon, z) ->
            Triple(lat.toScale(6), lon.toScale(6), z)
        }
    }
}
