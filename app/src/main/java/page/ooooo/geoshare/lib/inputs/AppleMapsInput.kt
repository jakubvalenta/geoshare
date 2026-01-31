package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.collections.immutable.ImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints
import page.ooooo.geoshare.lib.point.toParseHtmlResult
import page.ooooo.geoshare.lib.point.toParseUriResult

object AppleMapsInput : Input.HasHtml {
    override val uriPattern = Regex("""(?:https?://)?maps\.apple(\.com)?[/?#]\S+""")
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
                // Notice that we take the search center 'sll' as a normal point
                @Suppress("SpellCheckingInspection")
                listOf("ll", "daddr", "coordinate", "q", "sll", "center")
                    .firstNotNullOfOrNull { key -> LAT_LON_PATTERN match queryParams[key] }
                    ?.toLatLonPoint()
                    ?.also { points.add(it) }

                @Suppress("SpellCheckingInspection")
                listOf("name", "address", "daddr", "q")
                    .firstNotNullOfOrNull { key -> Q_PARAM_PATTERN match queryParams[key] }
                    ?.groupOrNull()
                    ?.also { defaultName = it }

                (Z_PATTERN match queryParams["z"])?.doubleGroupOrNull()?.also { defaultZ = it }

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

            val latPattern = Regex("""<meta property="place:location:latitude" content="$LAT"""")
            val lonPattern = Regex("""<meta property="place:location:longitude" content="$LON"""")
            var lat: Double? = null
            var lon: Double? = null
            while (true) {
                val line = channel.readUTF8Line() ?: break
                if (lat == null) {
                    (latPattern find line)?.doubleGroupOrNull()?.let { lat = it }
                }
                if (lon == null) {
                    (lonPattern find line)?.doubleGroupOrNull()?.let { lon = it }
                }
                if (lat != null && lon != null) {
                    points.add(NaivePoint(lat, lon))
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
