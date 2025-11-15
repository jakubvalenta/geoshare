package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import kotlinx.io.Source
import kotlinx.io.readLine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.*
import page.ooooo.geoshare.lib.position.LatLonZ
import page.ooooo.geoshare.lib.position.PositionBuilder
import page.ooooo.geoshare.lib.position.Srs

object AppleMapsInput : Input.HasHtml {
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

    override fun parseUri(uri: Uri) = uri.run {
        PositionBuilder(srs).apply {
            setPointIfNull { LAT_LON_PATTERN matchLatLonZ queryParams["ll"] }
            setPointIfNull { LAT_LON_PATTERN matchLatLonZ queryParams["coordinate"] }
            setPointIfNull { LAT_LON_PATTERN matchLatLonZ queryParams["q"] }
            setQIfNull { Q_PARAM_PATTERN matchQ queryParams["address"] }
            setQOrNameIfEmpty { Q_PARAM_PATTERN matchQ queryParams["name"] }
            setQWithCenterIfNull {
                (Q_PARAM_PATTERN matchQ queryParams["q"])?.let { newQ ->
                    (LAT_LON_PATTERN matchLatLonZ queryParams["sll"])?.let { (lat, lon) ->
                        Triple(newQ, lat, lon)
                    }
                }
            }
            setPointIfNull { LAT_LON_PATTERN matchLatLonZ queryParams["sll"] }
            setPointIfNull { LAT_LON_PATTERN matchLatLonZ queryParams["center"] }
            setQIfNull { Q_PARAM_PATTERN matchQ queryParams["q"] }
            setZIfNull { (Z_PATTERN matchZ queryParams["z"]) }
            setUriStringIfNull {
                uri.takeIf {
                    host == "maps.apple" && path.startsWith("/p/") ||
                        @Suppress("SpellCheckingInspection")
                        !queryParams["auid"].isNullOrEmpty() ||
                        !queryParams["place-id"].isNullOrEmpty()
                }?.toString()
            }
        }.toPair()
    }

    override fun parseHtml(source: Source) = source.run {
        PositionBuilder(srs).apply {
            val latPattern = Pattern.compile("""<meta property="place:location:latitude" content="$LAT"""")
            val lonPattern = Pattern.compile("""<meta property="place:location:longitude" content="$LON"""")
            var lat: Double? = null
            var lon: Double? = null
            for (line in generateSequence { source.readLine() }) {
                if (lat == null) {
                    (latPattern find line)?.toLat()?.let { lat = it }
                }
                if (lon == null) {
                    (lonPattern find line)?.toLon()?.let { lon = it }
                }
                if (lat != null && lon != null) {
                    addPoint { LatLonZ(lat, lon, null) }
                    break
                }
            }
        }.toPair()
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_apple_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_apple_maps_loading_indicator_title
}
