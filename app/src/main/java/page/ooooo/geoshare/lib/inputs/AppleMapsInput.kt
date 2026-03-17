package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.WGS84Point

object AppleMapsInput : HtmlInput, Input.HasRandomUri {
    override val uriPattern = Regex("""(?:https?://)?maps\.apple(\.com)?[/?#]\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.APPLE_MAPS,
        nameResId = R.string.converter_apple_maps_name,
        items = listOf(
            InputDocumentationItem.Url(18, "https://maps.apple"),
            InputDocumentationItem.Url(18, "https://maps.apple.com"),
        ),
    )

    override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = buildParseUriResult {
        uri.run {
            val z = Z_PATTERN.matchEntire(queryParams["z"])?.doubleGroupOrNull()

            // Search or place with name
            // https://maps.apple.com/?q={name}
            // https://maps.apple.com/place?place-id={id}...&q={name}
            val name = listOf("name", "address", @Suppress("SpellCheckingInspection") "daddr", "q")
                .firstNotNullOfOrNull { key -> Q_PARAM_PATTERN.matchEntire(queryParams[key])?.groupOrNull() }

            // Coordinates
            // https://maps.apple.com/?ll={lat},{lon}
            // Notice that we consider the search center 'sll' to be a normal point
            listOf("ll", @Suppress("SpellCheckingInspection") "daddr", "coordinate", "q", "sll", "center")
                .firstNotNullOfOrNull { key -> LAT_LON_PATTERN.matchEntire(queryParams[key])?.toLatLonPoint() }?.let {
                    points = persistentListOf(it.asWGS84().copy(z = z, name = name))
                    return@run
                }

            // Short link
            // https://maps.apple/p/{hash}
            if (host == "maps.apple" && path.startsWith("/p/") ||
                // Place
                // https://maps.apple.com/place?auid={id}...
                !queryParams[@Suppress("SpellCheckingInspection") "auid"].isNullOrEmpty() ||
                // Place
                // https://maps.apple.com/place?place-id={id}...
                !queryParams["place-id"].isNullOrEmpty()
            ) {
                htmlUriString = toString()
            }

            if (name != null) {
                points = persistentListOf(WGS84Point(z = z, name = name))
            }
        }
    }

    override suspend fun parseHtml(
        htmlUrlString: String,
        channel: ByteReadChannel,
        pointsFromUri: ImmutableList<Point>,
        uriQuote: UriQuote,
        log: ILog,
    ) = buildParseHtmlResult {
        val name = pointsFromUri.lastOrNull()?.name

        val latPattern = Regex("""<meta property="place:location:latitude" content="$LAT"""")
        val lonPattern = Regex("""<meta property="place:location:longitude" content="$LON"""")
        var lat: Double? = null
        var lon: Double? = null
        while (true) {
            val line = channel.readLine() ?: break
            if (lat == null) {
                latPattern.find(line)?.doubleGroupOrNull()?.let { lat = it }
            }
            if (lon == null) {
                lonPattern.find(line)?.doubleGroupOrNull()?.let { lon = it }
            }
            if (lat != null && lon != null) {
                points = persistentListOf(WGS84Point(lat, lon, name = name))
                return@buildParseHtmlResult
            }
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_apple_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_apple_maps_loading_indicator_title

    override fun genRandomUri(point: Point) =
        point.formatUriString(
            listOf(
                "https://maps.apple.com/?ll={lat}%2C{lon}&z={z}&q={name}",
                "https://maps.apple.com/?daddr={lat}%2C{lon}",
            ).random()
        )
}
