package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.geo.decodeWazeGeoHash
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.WGS84Point

/**
 * See https://developers.google.com/waze/deeplinks/
 */
object WazeInput : HtmlInput, Input.HasRandomUri {
    @Suppress("SpellCheckingInspection")
    private const val HASH = """[0-9bcdefghjkmnpqrstuvwxyz]+"""

    override val uriPattern = Regex("""(?:https?://)?(?:(?:www|ul)\.)?waze\.com/$URI_REST""")

    override val documentation = InputDocumentation(
        id = InputDocumentationId.WAZE,
        nameResId = R.string.converter_waze_name,
        items = listOf(
            InputDocumentationItem.Url(21, "https://waze.com/live-map"),
            InputDocumentationItem.Url(21, "https://waze.com/ul"),
            InputDocumentationItem.Url(21, "https://www.waze.com/live-map"),
            InputDocumentationItem.Url(21, "https://www.waze.com/ul"),
            InputDocumentationItem.Url(21, "https://ul.waze.com/ul"),
        ),
    )

    override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = buildParseUriResult {
        uri.run {
            // Short link
            // https://waze.com/ul/h{hash}
            (Regex("""/ul/h($HASH)""").matchEntire(path)
            // https://www.waze.com/live-map?h={hash}
                ?: Regex("($HASH)").matchEntire(queryParams["h"])
                )?.groupOrNull()
                ?.let { hash -> decodeWazeGeoHash(hash) }
                ?.let {
                    points = persistentListOf(it.asWGS84().copy(lat = it.lat?.toScale(6), lon = it.lon?.toScale(6)))
                    return@run
                }

            val z = Z_PATTERN.matchEntire(queryParams["z"])?.doubleGroupOrNull()

            val name = Q_PARAM_PATTERN.matchEntire(queryParams["q"])?.groupOrNull()

            // Coordinates
            // https://waze.com/ul?ll={lat},{lon}
            (Regex("""ll\.$LAT,$LON""").matchEntire(queryParams["to"])
                ?: LAT_LON_PATTERN.matchEntire(queryParams["ll"])
                ?: LAT_LON_PATTERN.matchEntire(queryParams[@Suppress("SpellCheckingInspection") "latlng"])
                )?.toLatLonPoint()?.let {
                    points = persistentListOf(it.asWGS84().copy(z = z, name = name))
                    return@run
                }

            // Search
            // https://waze.com/ul?q={name}
            if (name != null) {
                points = persistentListOf(WGS84Point(z = z, name = name))
            }

            // Place
            // https://ul.waze.com/ul?venue_id={id}
            queryParams["venue_id"]?.takeIf { it.isNotEmpty() }?.let { venueId ->
                // To skip some redirects when downloading HTML, replace this URL:
                // https://ul.waze.com/ul?venue_id=2884104.28644432.6709020
                // or this URL:
                // https://www.waze.com/ul?venue_id=2884104.28644432.6709020
                // with this one:
                // https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020
                htmlUriString = Uri(
                    scheme = "https",
                    host = "www.waze.com",
                    path = "/live-map/directions",
                    queryParams = persistentMapOf("to" to "place.w.$venueId"),
                    uriQuote = uri.uriQuote,
                ).toString()
            } ?: queryParams["place"]?.takeIf { it.isNotEmpty() }?.let { placeId ->
                // To skip some redirects when downloading HTML, replace this URL:
                // https://www.waze.com/live-map/directions?place=w.2884104.28644432.6709020
                // with this one:
                // https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020
                htmlUriString = Uri(
                    scheme = "https",
                    host = "www.waze.com",
                    path = "/live-map/directions",
                    queryParams = persistentMapOf("to" to "place.$placeId"),
                    uriQuote = uri.uriQuote,
                ).toString()
            } ?: queryParams["to"]?.takeIf { it.startsWith("place.") }?.let {
                htmlUriString = toString()
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

        val pattern = Regex(""""latLng":\{"lat":$LAT,"lng":$LON\}""")
        while (true) {
            val line = channel.readLine() ?: break
            pattern.find(line)?.toLatLonPoint()?.let {
                points = persistentListOf(it.asWGS84().copy(name = name))
                return@buildParseHtmlResult
            }
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_waze_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_waze_loading_indicator_title

    override fun genRandomUri(point: Point) =
        point.formatUriString("https://waze.com/ul?ll={lat}%2C{lon}&z={z}")
}
