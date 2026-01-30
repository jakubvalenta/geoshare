package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentMapOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.findNaivePoint
import page.ooooo.geoshare.lib.extensions.matchHash
import page.ooooo.geoshare.lib.extensions.matchNaivePoint
import page.ooooo.geoshare.lib.extensions.matchQ
import page.ooooo.geoshare.lib.extensions.matchZ
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.geo.decodeWazeGeoHash
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints
import page.ooooo.geoshare.lib.point.toParseHtmlResult
import page.ooooo.geoshare.lib.point.toParseUriResult

/**
 * See https://developers.google.com/waze/deeplinks/
 */
object WazeInput : Input.HasHtml {
    @Suppress("SpellCheckingInspection")
    private const val HASH = """(?P<hash>[0-9bcdefghjkmnpqrstuvwxyz]+)"""

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?((www|ul)\.)?waze\.com/\S+""")

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

    override suspend fun parseUri(uri: Uri): ParseUriResult? {
        var htmlUriString: String? = null
        return buildPoints {
            uri.run {
                (("""/ul/h$HASH""" matchHash path) ?: (HASH matchHash queryParams["h"]))
                    ?.let { hash -> decodeWazeGeoHash(hash) }
                    ?.let { (lat, lon, z) -> NaivePoint(lat.toScale(6), lon.toScale(6), z) }
                    ?.also { points.add(it) }
                    ?: ("""ll\.$LAT,$LON""" matchNaivePoint queryParams["to"])?.also { points.add(it) }
                    ?: (LAT_LON_PATTERN matchNaivePoint queryParams["ll"])?.also { points.add(it) }
                    ?: (LAT_LON_PATTERN matchNaivePoint queryParams[@Suppress("SpellCheckingInspection") "latlng"])
                        ?.also { points.add(it) }

                (Q_PARAM_PATTERN matchQ queryParams["q"])?.let { defaultName = it }

                (Z_PATTERN matchZ queryParams["z"])?.let { defaultZ = it }

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

            val pattern = Pattern.compile(""""latLng":{"lat":$LAT,"lng":$LON}""")
            while (true) {
                val line = channel.readUTF8Line() ?: break
                (pattern findNaivePoint line)?.also {
                    points.add(it)
                    break
                }
            }
        }
            .asWGS84()
            .toParseHtmlResult()

    @StringRes
    override val permissionTitleResId = R.string.converter_waze_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_waze_loading_indicator_title
}
