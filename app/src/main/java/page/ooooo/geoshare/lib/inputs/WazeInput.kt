package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentMapOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.geo.decodeWazeGeoHash
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints

/**
 * See https://developers.google.com/waze/deeplinks/
 */
object WazeInput : Input.HasHtml {
    @Suppress("SpellCheckingInspection")
    private const val HASH = """[0-9bcdefghjkmnpqrstuvwxyz]+"""

    override val uriPattern = Regex("""(?:https?://)?(?:(?:www|ul)\.)?waze\.com/\S+""")

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

    override suspend fun parseUri(uri: Uri) = buildParseUriResult {
        points = buildPoints {
            uri.run {
                (Regex("""/ul/h($HASH)""").matchEntire(path) ?: Regex("($HASH)").matchEntire(queryParams["h"]))
                    ?.groupOrNull()
                    ?.let { hash -> decodeWazeGeoHash(hash) }
                    ?.let { (lat, lon, z) -> NaivePoint(lat.toScale(6), lon.toScale(6), z) }
                    ?.also { points.add(it) }
                    ?: Regex("""ll\.$LAT,$LON""").matchEntire(queryParams["to"])?.toLatLonPoint()
                        ?.also { points.add(it) }
                    ?: LAT_LON_PATTERN.matchEntire(queryParams["ll"])
                        ?.toLatLonPoint()
                        ?.also { points.add(it) }
                    ?: LAT_LON_PATTERN.matchEntire(queryParams[@Suppress("SpellCheckingInspection") "latlng"])
                        ?.toLatLonPoint()
                        ?.also { points.add(it) }

                Q_PARAM_PATTERN.matchEntire(queryParams["q"])?.groupOrNull()?.also { defaultName = it }

                Z_PATTERN.matchEntire(queryParams["z"])?.doubleGroupOrNull()?.also { defaultZ = it }

                if (points.isEmpty()) {
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
                        htmlUriString = uri.toString()
                    }
                }
            }
        }.asWGS84()
    }

    override suspend fun parseHtml(
        htmlUrlString: String,
        channel: ByteReadChannel,
        pointsFromUri: ImmutableList<Point>,
        log: ILog,
    ) = buildParseHtmlResult {
        points = buildPoints {
            defaultName = pointsFromUri.lastOrNull()?.name

            val pattern = Regex(""""latLng":\{"lat":$LAT,"lng":$LON\}""")
            while (true) {
                val line = channel.readLine() ?: break
                pattern.find(line)?.toLatLonPoint()?.also {
                    points.add(it)
                    break
                }
            }
        }.asWGS84()
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_waze_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_waze_loading_indicator_title
}
