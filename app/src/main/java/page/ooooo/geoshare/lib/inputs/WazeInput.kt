package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.io.Source
import kotlinx.io.readLine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.decodeWazeGeoHash
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.position.*

/**
 * See https://developers.google.com/waze/deeplinks/
 */
object WazeInput : Input.HasHtml {
    @Suppress("SpellCheckingInspection")
    private const val HASH = """(?P<hash>[0-9bcdefghjkmnpqrstuvwxyz]+)"""

    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?((www|ul)\.)?waze\.com/\S+""")

    override val documentation = Input.Documentation(
        nameResId = R.string.converter_waze_name,
        inputs = listOf(
            Input.DocumentationInput.Url(21, "https://waze.com/live-map"),
            Input.DocumentationInput.Url(21, "https://waze.com/ul"),
            Input.DocumentationInput.Url(21, "https://www.waze.com/live-map"),
            Input.DocumentationInput.Url(21, "https://www.waze.com/ul"),
            Input.DocumentationInput.Url(21, "https://ul.waze.com/ul"),
        ),
    )

    override fun parseUri(uri: Uri) = uri.run {
        PositionBuilder(srs).apply {
            setLatLonZoom {
                (("""/ul/h$HASH""" match path) ?: (HASH match queryParams["h"]))?.groupOrNull("hash")
                    ?.let { hash -> decodeWazeGeoHash(hash) }
                    ?.let { (lat, lon, z) -> Triple(lat.toScale(6), lon.toScale(6), z) }
            }
            setPointFromMatcher { """ll\.$LAT,$LON""" match queryParams["to"] }
            setPointFromMatcher { LAT_LON_PATTERN match queryParams["ll"] }
            @Suppress("SpellCheckingInspection")
            setPointFromMatcher { LAT_LON_PATTERN match queryParams["latlng"] }
            setQueryFromMatcher { Q_PARAM_PATTERN match queryParams["q"] }
            setZoomFromMatcher { Z_PATTERN match queryParams["z"] }
            setUriString {
                queryParams["venue_id"]?.takeIf { it.isNotEmpty() }?.let { venueId ->
                    // To skip some redirects when downloading HTML, replace this URL:
                    // https://ul.waze.com/ul?venue_id=183894452.1839010060.260192
                    // or this URL:
                    // https://www.waze.com/ul?venue_id=183894452.1839010060.260192
                    // with this one:
                    // https://www.waze.com/live-map/directions?to=place.w.183894452.1839010060.260192
                    Uri(
                        scheme = "https",
                        host = "www.waze.com",
                        path = "/live-map/directions",
                        queryParams = persistentMapOf("to" to "place.w.$venueId"),
                        uriQuote = uri.uriQuote,
                    ).toString()
                }
            }
            setUriString {
                queryParams["place"]?.takeIf { it.isNotEmpty() }?.let { placeId ->
                    // To skip some redirects when downloading HTML, replace this URL:
                    // https://www.waze.com/live-map/directions?place=w.183894452.1839010060.260192
                    // with this one:
                    // https://www.waze.com/live-map/directions?to=place.w.183894452.1839010060.260192
                    Uri(
                        scheme = "https",
                        host = "www.waze.com",
                        path = "/live-map/directions",
                        queryParams = persistentMapOf("to" to "place.$placeId"),
                        uriQuote = uri.uriQuote,
                    ).toString()
                }
            }
            setUriString { if (queryParams["to"]?.startsWith("place.") == true) uri.toString() else null }
        }.toPair()
    }

    override fun parseHtml(source: Source) = source.run {
        PositionBuilder(srs).apply {
            val pattern = Pattern.compile(""""latLng":{"lat":$LAT,"lng":$LON}""")
            for (line in generateSequence { source.readLine() }) {
                (pattern find line)?.toPoint(srs)?.let { point ->
                    points.add(point)
                    break
                }
            }
        }.toPair()
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_waze_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_waze_loading_indicator_title
}
