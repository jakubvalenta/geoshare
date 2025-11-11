package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
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
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matches
import page.ooooo.geoshare.lib.position.Srs

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

    override val conversionUriPattern = conversionPattern<Uri, IncompletePosition> {
        all {
            optional {
                on { queryParams["z"]?.let { it matches Z }?.toIncompleteZPosition(srs) }
            }
            first {
                on { if (host == "maps.apple") (path matches "/p/.+")?.let { IncompletePosition(srs) } else null }
                on { queryParams["ll"]?.let { it matches "$LAT,$LON" }?.toIncompleteLatLonPosition(srs) }
                on { queryParams["coordinate"]?.let { it matches "$LAT,$LON" }?.toIncompleteLatLonPosition(srs) }
                on { queryParams["q"]?.let { it matches "$LAT,$LON" }?.toIncompleteLatLonPosition(srs) }
                on { queryParams["address"]?.let { it matches Q_PARAM }?.toIncompleteQPosition(srs) }
                on { queryParams["name"]?.let { it matches Q_PARAM }?.toIncompleteQPosition(srs) }
                @Suppress("SpellCheckingInspection")
                on { queryParams["auid"]?.let { it matches ".+" }?.let { IncompletePosition(srs) } }
                on { queryParams["place-id"]?.let { it matches ".+" }?.let { IncompletePosition(srs) } }
                all {
                    on { queryParams["q"]?.let { it matches Q_PARAM }?.toIncompleteQPosition(srs) }
                    on { queryParams["sll"]?.let { it matches "$LAT,$LON" }?.toIncompleteLatLonPosition(srs) }
                }
                on { queryParams["sll"]?.let { it matches "$LAT,$LON" }?.toIncompleteLatLonPosition(srs) }
                on { queryParams["center"]?.let { it matches "$LAT,$LON" }?.toIncompleteLatLonPosition(srs) }
                on {
                    // Set point to 0,0 to avoid parsing HTML for this URI. Because parsing HTML for this URI doesn't work.
                    queryParams["q"]?.let { it matches Q_PARAM }?.toIncompleteQPosition(srs)?.copy(lat = 0.0, lon = 0.0)
                }
            }
        }
    }

    override val conversionHtmlPattern = conversionPattern<Source, IncompletePosition> {
        on {
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
                    IncompletePosition(srs, lat, lon)
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
