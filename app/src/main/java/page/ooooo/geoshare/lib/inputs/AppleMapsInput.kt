package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import kotlinx.io.Source
import kotlinx.io.readLine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.position.*

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
            setPointFromMatcher { LAT_LON_PATTERN match queryParams["sll"] }
            setPointFromMatcher { LAT_LON_PATTERN match queryParams["center"] }
            setPointFromMatcher { LAT_LON_PATTERN match queryParams["ll"] }
            setPointFromMatcher { LAT_LON_PATTERN match queryParams["coordinate"] }
            setPointFromMatcher { LAT_LON_PATTERN match queryParams["q"] }
            setQueryFromMatcher { Q_PARAM_PATTERN match queryParams["address"] }
            setQueryFromMatcher { Q_PARAM_PATTERN match queryParams["name"] }
            setQueryFromMatcher { Q_PARAM_PATTERN match queryParams["q"] }
            setZoomFromMatcher { (Z_PATTERN match queryParams["z"]) }
            setUrl { if (host == "maps.apple" && path.startsWith("/p/")) uri.toUrl() else null }
            @Suppress("SpellCheckingInspection")
            setUrl { if (!queryParams["auid"].isNullOrEmpty()) uri.toUrl() else null }
            setUrl { if (!queryParams["place-id"].isNullOrEmpty()) uri.toUrl() else null }
        }
    }

    override fun parseHtml(source: Source) = source.run {
        PositionBuilder(srs).apply {
            val latPattern = Pattern.compile("""<meta property="place:location:latitude" content="$LAT"""")
            val lonPattern = Pattern.compile("""<meta property="place:location:longitude" content="$LON"""")
            var lat: Double? = null
            var lon: Double? = null
            for (line in generateSequence { source.readLine() }) {
                if (lat == null) {
                    (latPattern find line)?.groupOrNull("lat")?.toDoubleOrNull()?.let { lat = it }
                }
                if (lon == null) {
                    (lonPattern find line)?.groupOrNull("lon")?.toDoubleOrNull()?.let { lon = it }
                }
                if (lat != null && lon != null) {
                    setLatLon { lat to lon }
                    break
                }
            }
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_apple_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_apple_maps_loading_indicator_title
}
