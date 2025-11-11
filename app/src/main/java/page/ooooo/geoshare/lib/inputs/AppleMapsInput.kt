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
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matches
import page.ooooo.geoshare.lib.position.*

object AppleMapsInput : Input.HasUri, Input.HasHtml {
    const val NAME = "Apple Maps"

    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?maps\.apple(\.com)?[/?#]\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_apple_maps_name,
        inputs = listOf(
            Input.DocumentationInput.Url(18, "https://maps.apple"),
            Input.DocumentationInput.Url(18, "https://maps.apple.com"),
        ),
    )

    override val conversionUriPattern = ConversionPattern.first<Uri, Position> {
        all {
            optional {
                pattern { queryParams["z"]?.let { it matches Z_PATTERN }?.toZ(srs) }
            }
            first {
                pattern { if (host == "maps.apple" && path.startsWith("/p/")) Position(srs) else null }
                pattern { queryParams["ll"]?.let { it matches LAT_LON_PATTERN }?.toLatLon(srs) }
                pattern { queryParams["coordinate"]?.let { it matches LAT_LON_PATTERN }?.toLatLon(srs) }
                pattern { queryParams["q"]?.let { it matches LAT_LON_PATTERN }?.toLatLon(srs) }
                pattern { queryParams["address"]?.let { it matches Q_PARAM_PATTERN }?.toQ(srs) }
                pattern { queryParams["name"]?.let { it matches Q_PARAM_PATTERN }?.toQ(srs) }
                @Suppress("SpellCheckingInspection")
                pattern { queryParams["auid"]?.isNotEmpty()?.let { Position(srs) } }
                pattern { queryParams["place-id"]?.isNotEmpty()?.let { Position(srs) } }
                all {
                    pattern { queryParams["q"]?.let { it matches Q_PARAM_PATTERN }?.toQ(srs) }
                    pattern { queryParams["sll"]?.let { it matches LAT_LON_PATTERN }?.toLatLon(srs) }
                }
                pattern { queryParams["sll"]?.let { it matches LAT_LON_PATTERN }?.toLatLon(srs) }
                pattern { queryParams["center"]?.let { it matches LAT_LON_PATTERN }?.toLatLon(srs) }
                pattern {
                    // Set point to 0,0 to avoid parsing HTML for this URI. Because parsing HTML for this URI doesn't work.
                    queryParams["q"]?.let { it matches Q_PARAM_PATTERN }?.groupOrNull("q")
                        ?.let { q -> Position(srs, lat = 0.0, lon = 0.0, q = q) }
                }
            }
        }
    }

    override val conversionHtmlPattern = ConversionPattern.first<Source, Position> {
        pattern {
            val latPattern = Pattern.compile("""<meta property="place:location:latitude" content="$LAT"""")
            var lonPattern = Pattern.compile("""<meta property="place:location:longitude" content="$LON"""")
            var lat: Double? = null
            var lon: Double? = null
            for (line in generateSequence { this.readLine() }) {
                if (lat == null) {
                    (line find latPattern)?.groupOrNull("lat")?.toDoubleOrNull()?.let { newLat ->
                        lat = newLat
                        if (lon != null) {
                            break
                        }
                    }
                }
                if (lon == null) {
                    (line find lonPattern)?.groupOrNull("lon")?.toDoubleOrNull()?.let { newLon ->
                        lon = newLon
                        if (lat != null) {
                            break
                        }
                    }
                }
            }
            lat?.let { lat ->
                lon?.let { lon ->
                    Position(srs, lat, lon)
                }
            }
        }
    }

    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_apple_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_apple_maps_loading_indicator_title
}
