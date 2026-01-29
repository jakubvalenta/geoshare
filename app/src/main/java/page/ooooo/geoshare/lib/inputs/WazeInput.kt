package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import io.ktor.utils.io.*
import kotlinx.collections.immutable.persistentMapOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.geo.decodeWazeGeoHash
import page.ooooo.geoshare.lib.extensions.*
import page.ooooo.geoshare.lib.position.LatLonZName
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.position.buildPosition

/**
 * See https://developers.google.com/waze/deeplinks/
 */
object WazeInput : Input.HasHtml {
    @Suppress("SpellCheckingInspection")
    private const val HASH = """(?P<hash>[0-9bcdefghjkmnpqrstuvwxyz]+)"""

    private val srs = Srs.WGS84

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
        val position = buildPosition(srs) {
            uri.run {
                setPointIfNull {
                    (("""/ul/h$HASH""" matchHash path) ?: (HASH matchHash queryParams["h"]))
                        ?.let { hash -> decodeWazeGeoHash(hash) }
                        ?.let { (lat, lon, z) -> LatLonZName(lat.toScale(6), lon.toScale(6), z) }
                }
                setPointIfNull { """ll\.$LAT,$LON""" matchLatLonZName queryParams["to"] }
                setPointIfNull { LAT_LON_PATTERN matchLatLonZName queryParams["ll"] }
                @Suppress("SpellCheckingInspection")
                setPointIfNull { LAT_LON_PATTERN matchLatLonZName queryParams["latlng"] }
                setQIfNull { Q_PARAM_PATTERN matchQ queryParams["q"] }
                setZIfNull { Z_PATTERN matchZ queryParams["z"] }
                if (!hasPoint()) {
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
        return ParseUriResult.from(position, htmlUriString)
    }

    override suspend fun parseHtml(channel: ByteReadChannel, positionFromUri: Position, log: ILog): ParseHtmlResult? {
        val positionFromHtml = buildPosition(srs) {
            val pattern = Pattern.compile(""""latLng":{"lat":$LAT,"lng":$LON}""")
            while (true) {
                val line = channel.readUTF8Line() ?: break
                if (setPointIfNull { pattern findLatLonZName line }) {
                    break
                }
            }
        }
        return ParseHtmlResult.from(positionFromUri, positionFromHtml)
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_waze_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_waze_loading_indicator_title
}
