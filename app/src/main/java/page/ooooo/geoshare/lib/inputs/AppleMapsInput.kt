package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.collections.immutable.ImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.matchNaivePoint
import page.ooooo.geoshare.lib.extensions.matchQ
import page.ooooo.geoshare.lib.extensions.matchZ
import page.ooooo.geoshare.lib.extensions.toLat
import page.ooooo.geoshare.lib.extensions.toLon
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints
import page.ooooo.geoshare.lib.point.toParseHtmlResult
import page.ooooo.geoshare.lib.point.toParseUriResult

object AppleMapsInput : Input.HasHtml {
    const val NAME = "Apple Maps"

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
        return buildPoints {
            uri.run {
                setPointIfNull { LAT_LON_PATTERN matchNaivePoint queryParams["ll"] }
                @Suppress("SpellCheckingInspection")
                setPointIfNull { LAT_LON_PATTERN matchNaivePoint queryParams["daddr"] }
                setPointIfNull { LAT_LON_PATTERN matchNaivePoint queryParams["coordinate"] }
                setPointIfNull { LAT_LON_PATTERN matchNaivePoint queryParams["q"] }
                setNameIfNull { Q_PARAM_PATTERN matchQ queryParams["name"] }
                setNameIfNull { Q_PARAM_PATTERN matchQ queryParams["address"] }
                @Suppress("SpellCheckingInspection")
                setNameIfNull { Q_PARAM_PATTERN matchQ queryParams["daddr"] }
                setPointIfNull { LAT_LON_PATTERN matchNaivePoint queryParams["sll"] }
                setPointIfNull { LAT_LON_PATTERN matchNaivePoint queryParams["center"] }
                setNameIfNull { Q_PARAM_PATTERN matchQ queryParams["q"] }
                setZIfNull { (Z_PATTERN matchZ queryParams["z"]) }
                if (
                    points.isEmpty() && (
                        host == "maps.apple" && path.startsWith("/p/") ||
                            @Suppress("SpellCheckingInspection")
                            !queryParams["auid"].isNullOrEmpty() ||
                            !queryParams["place-id"].isNullOrEmpty())
                ) {
                    htmlUriString = uri.toString()
                }
            }
        }
            .asWGS84()
            .toParseUriResult(htmlUriString)
    }

    override suspend fun parseHtml(
        channel: ByteReadChannel,
        pointsFromUri: ImmutableList<Point>,
        log: ILog,
    ): ParseHtmlResult? =
        buildPoints {
            defaultName = pointsFromUri.lastOrNull()?.name
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
                    setPointIfNull { NaivePoint(lat, lon) }
                    break
                }
            }
        }
            .asWGS84()
            .toParseHtmlResult()

    @StringRes
    override val permissionTitleResId = R.string.converter_apple_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_apple_maps_loading_indicator_title
}
