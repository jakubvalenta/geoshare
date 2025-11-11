package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import kotlinx.io.Source
import kotlinx.io.readLine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ConversionPattern
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LAT
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LAT_LON_PATTERN
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LON
import page.ooooo.geoshare.lib.ConversionPattern.Companion.Q_PARAM_PATTERN
import page.ooooo.geoshare.lib.ConversionPattern.Companion.Z_PATTERN
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.decodeWazeGeoHash
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matches
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.position.*

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

    override val conversionUriPattern = ConversionPattern.first<Uri, Position> {
        all {
            optional {
                pattern { queryParams["z"]?.let { it matches Z_PATTERN }?.toZ(srs) }
            }
            first {
                pattern {
                    ((path matches """/ul/h$HASH""") ?: queryParams["h"]?.let { it matches HASH })?.groupOrNull("hash")
                        ?.let { hash ->
                            decodeWazeGeoHash(hash).let { (lat, lon, z) ->
                                Position(srs, lat = lat.toScale(6), lon = lon.toScale(6), z = z)
                            }
                        }
                }
                pattern { queryParams["to"]?.let { it matches """ll\.$LAT,$LON""" }?.toLatLon(srs) }
                pattern { queryParams["ll"]?.let { it matches LAT_LON_PATTERN }?.toLatLon(srs) }
                @Suppress("SpellCheckingInspection")
                pattern { queryParams["latlng"]?.let { it matches LAT_LON_PATTERN }?.toLatLon(srs) }
                pattern { queryParams["q"]?.let { it matches Q_PARAM_PATTERN }?.toQ(srs) }
                pattern { queryParams["venue_id"]?.isNotEmpty()?.let { Position(srs) } }
                pattern { queryParams["place"]?.isNotEmpty()?.let { Position(srs) } }
                pattern { queryParams["to"]?.takeIf { it.startsWith("place.") }?.let { Position(srs) } }
            }
        }
    }

    override val conversionHtmlPattern = ConversionPattern.first<Source, Position> {
        pattern {
            val pattern = Pattern.compile(""""latLng":{"lat":$LAT,"lng":$LON}""")
            generateSequence { this.readLine() }
                .firstNotNullOfOrNull { line -> line find pattern }
                ?.toLatLon(srs)
        }
    }

    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_waze_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_waze_loading_indicator_title
}
