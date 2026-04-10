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
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.Source
import page.ooooo.geoshare.lib.point.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppleMapsInput @Inject constructor(
    private val uriFormatter: UriFormatter,
) : HtmlInput, Input.HasRandomUri {
    override val uriPattern = Regex("""(?:https?://)?maps\.apple(\.com)?[/?#]$URI_REST""")
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
            listOf("ll", @Suppress("SpellCheckingInspection") "daddr", "coordinate", "q")
                .firstNotNullOfOrNull { key ->
                    LAT_LON_PATTERN.matchEntire(queryParams[key])?.toLatLonPoint(Source.URI)
                }?.let {
                    points = persistentListOf(WGS84Point(it).copy(z = z, name = name))
                    return@run
                }

            // Map center (including the search center 'sll')
            // https://maps.apple.com/?center={lat},{lon}
            listOf("sll", "center")
                .firstNotNullOfOrNull { key ->
                    LAT_LON_PATTERN.matchEntire(queryParams[key])?.toLatLonPoint(Source.MAP_CENTER)
                }?.let {
                    points = persistentListOf(WGS84Point(it).copy(z = z, name = name))
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
                points = persistentListOf(WGS84Point(z = z, name = name, source = Source.URI))
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
        val latPattern = Regex("""<meta property="place:location:latitude" content="$LAT"""")
        val lonPattern = Regex("""<meta property="place:location:longitude" content="$LON"""")

        var lat: Double? = null
        var lon: Double? = null
        val name = pointsFromUri.lastOrNull()?.name

        while (true) {
            val line = channel.readLine() ?: break
            if (lat == null) {
                latPattern.find(line)?.doubleGroupOrNull()?.let { lat = it }
            }
            if (lon == null) {
                lonPattern.find(line)?.doubleGroupOrNull()?.let { lon = it }
            }
            if (lat != null && lon != null) {
                points = persistentListOf(WGS84Point(lat, lon, name = name, source = Source.HTML))
                return@buildParseHtmlResult
            }
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_apple_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_apple_maps_loading_indicator_title

    override fun genRandomUri(point: Point) =
        uriFormatter.formatUriString(
            point,
            listOf(
                "https://maps.apple.com/?ll={lat}%2C{lon}&z={z}&q={name}",
                "https://maps.apple.com/?daddr={lat}%2C{lon}",
            ).random(),
        )
}
