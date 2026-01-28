package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import io.ktor.utils.io.*
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.*
import page.ooooo.geoshare.lib.position.LatLonZName
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.position.buildPosition

object AppleMapsInput : Input.HasHtml {
    const val NAME = "Apple Maps"

    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?maps\.apple(\.com)?[/?#]\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.APPLE_MAPS,
        nameResId = R.string.converter_apple_maps_name,
        items = listOf(
            InputDocumentationItem.Url(18, "https://maps.apple"),
            InputDocumentationItem.Url(18, "https://maps.apple.com"),
        ),
    )

    override suspend fun parseUri(uri: Uri): ParseUriResult? {
        var htmlUriString: String? = null
        val position = buildPosition(srs) {
            uri.run {
                setPointIfNull { LAT_LON_PATTERN matchLatLonZName queryParams["ll"] }
                @Suppress("SpellCheckingInspection")
                setPointIfNull { LAT_LON_PATTERN matchLatLonZName queryParams["daddr"] }
                setPointIfNull { LAT_LON_PATTERN matchLatLonZName queryParams["coordinate"] }
                setPointIfNull { LAT_LON_PATTERN matchLatLonZName queryParams["q"] }
                setQIfNull { Q_PARAM_PATTERN matchQ queryParams["address"] }
                @Suppress("SpellCheckingInspection")
                setQIfNull { Q_PARAM_PATTERN matchQ queryParams["daddr"] }
                setQOrNameIfEmpty { Q_PARAM_PATTERN matchQ queryParams["name"] }
                setQWithCenterIfNull {
                    (Q_PARAM_PATTERN matchQ queryParams["q"])?.let { newQ ->
                        (LAT_LON_PATTERN matchLatLonZName queryParams["sll"])?.let { (lat, lon) ->
                            Triple(newQ, lat, lon)
                        }
                    }
                }
                setPointIfNull { LAT_LON_PATTERN matchLatLonZName queryParams["sll"] }
                setPointIfNull { LAT_LON_PATTERN matchLatLonZName queryParams["center"] }
                setQIfNull { Q_PARAM_PATTERN matchQ queryParams["q"] }
                setZIfNull { (Z_PATTERN matchZ queryParams["z"]) }
                if (
                    !hasPoint() && (
                        host == "maps.apple" && path.startsWith("/p/") ||
                            @Suppress("SpellCheckingInspection")
                            !queryParams["auid"].isNullOrEmpty() ||
                            !queryParams["place-id"].isNullOrEmpty())
                ) {
                    htmlUriString = uri.toString()
                }
            }
        }
        return ParseUriResult.from(position, htmlUriString)
    }

    override suspend fun parseHtml(channel: ByteReadChannel, positionFromUri: Position, log: ILog): ParseHtmlResult? {
        val positionFromHtml = buildPosition(srs) {
            val latPattern = Pattern.compile("""<meta property="place:location:latitude" content="$LAT"""")
            val lonPattern = Pattern.compile("""<meta property="place:location:longitude" content="$LON"""")
            var lat: Double? = null
            var lon: Double? = null
            while (true) {
                val line = channel.readUTF8Line() ?: break
                if (lat == null) {
                    (latPattern find line)?.toLat()?.let { lat = it }
                }
                if (lon == null) {
                    (lonPattern find line)?.toLon()?.let { lon = it }
                }
                if (lat != null && lon != null) {
                    setPointIfNull { LatLonZName(lat, lon) }
                    break
                }
            }
        }
        return ParseHtmlResult.from(positionFromUri, positionFromHtml)
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_apple_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_apple_maps_loading_indicator_title
}
