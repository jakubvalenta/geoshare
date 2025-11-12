package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import kotlinx.io.Source
import kotlinx.io.readLine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.conversion.ConversionPattern
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.LAT
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.LAT_LON_PATTERN
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.LON
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.Q_PARAM_PATTERN
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.Z_PATTERN
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.decodeWazeGeoHash
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
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
    // 1 https://ul.waze.com/ul?venue_id=183894452.1839010060.260192
    // 2 https://www.waze.com/ul?venue_id=183894452.1839010060.260192
    // 3 https://www.waze.com/live-map/directions?place=w.183894452.1839010060.260192
    // 4 https://www.waze.com/live-map/directions?to=place.w.183894452.1839010060.260192
    // TODO override val uriReplacement: String =

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
                pattern { (Z_PATTERN match queryParams["z"])?.toZ(srs) }
            }
            first {
                pattern {
                    (("""/ul/h$HASH""" match path) ?: (HASH match queryParams["h"]))?.groupOrNull("hash")?.let { hash ->
                        decodeWazeGeoHash(hash).let { (lat, lon, z) ->
                            Position(srs, lat = lat.toScale(6), lon = lon.toScale(6), z = z)
                        }
                    }
                }
                pattern { ("""ll\.$LAT,$LON""" match queryParams["to"])?.toLatLon(srs) }
                pattern { (LAT_LON_PATTERN match queryParams["ll"])?.toLatLon(srs) }
                @Suppress("SpellCheckingInspection")
                pattern { (LAT_LON_PATTERN match queryParams["latlng"])?.toLatLon(srs) }
                pattern { (Q_PARAM_PATTERN match queryParams["q"])?.toQ(srs) }
                pattern { if (!queryParams["venue_id"].isNullOrEmpty()) Position(srs) else null }
                pattern { if (!queryParams["place"].isNullOrEmpty()) Position(srs) else null }
                pattern { if (queryParams["to"]?.startsWith("place.") == true) Position(srs) else null }
            }
        }
    }

    override val conversionHtmlPattern = ConversionPattern.first<Source, Position> {
        pattern {
            val pattern = Pattern.compile(""""latLng":{"lat":$LAT,"lng":$LON}""")
            generateSequence { this.readLine() }
                .firstNotNullOfOrNull { line -> pattern find line }
                ?.toLatLon(srs)
        }
    }

    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_waze_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_waze_loading_indicator_title
}
