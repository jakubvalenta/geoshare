package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.geo.decodeWazeGeoHash
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
import page.ooooo.geoshare.lib.uri.toQueryParams
import javax.inject.Inject
import javax.inject.Singleton

/**
 * See https://developers.google.com/waze/deeplinks/
 */
@Singleton
class WazeUriInput @Inject constructor(
    private val wazeApiInput: dagger.Lazy<WazeApiInput>,
    private val wazeHtmlInput: dagger.Lazy<WazeHtmlInput>,
    override val uriQuote: UriQuote,
) : UriInput, Input.HasRandomUri {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.WAZE
    override val changelog = persistentListOf(
        InputChangelogItem.Url(21, "https://waze.com/live-map"),
        InputChangelogItem.Url(21, "https://waze.com/ul"),
        InputChangelogItem.Url(21, "https://www.waze.com/live-map"),
        InputChangelogItem.Url(21, "https://www.waze.com/ul"),
        InputChangelogItem.Url(21, "https://ul.waze.com/ul"),
    )

    override val pattern = Regex("""((?:https?://)?(?:(?:www|ul)\.)?waze\.com/$URI_REST)""")

    override suspend fun parse(data: Uri, match: String, resources: Resources) = parseResult {
        data.run {
            // Short link
            val hash = if (pathParts.getOrNull(1) == "ul") {
                // Hash in path
                // https://waze.com/ul/h{hash}
                Regex("""h($HASH)""").matchEntire(pathParts.getOrNull(2))?.groupOrNull()
            } else {
                // Hash in query parameter
                // https://www.waze.com/live-map?h={hash}
                Regex("($HASH)").matchEntire(queryParams["h"]?.firstOrNull())?.groupOrNull()
            }
            hash
                ?.let { hash -> decodeWazeGeoHash(hash) }
                ?.let {
                    points = persistentListOf(
                        WGS84Point(
                            lat = it.lat?.toScale(6),
                            lon = it.lon?.toScale(6),
                            z = it.z,
                            name = it.name,
                            source = it.source,
                        )
                    )
                    return@run
                }

            if (queryParams["a"]?.firstOrNull() == "share_drive") {
                // Route with token in the param 'sd'
                // https://www.waze.com/ul?a=share_drive&sd={token}
                queryParams["sd"]?.firstOrNull()?.let { token ->
                    next = MatchedInput(wazeApiInput.get(), token)
                    return@parseResult
                }
            } else if (pathParts.getOrNull(2) == "meeting") {
                // Route with token in the param 'token'
                // https://www.waze.com/live-map/meeting?token={token}
                queryParams["token"]?.firstOrNull()?.let { token ->
                    next = MatchedInput(wazeApiInput.get(), token)
                    return@parseResult
                }
            }

            val z = Z_PATTERN.matchEntire(queryParams["z"]?.firstOrNull())?.doubleGroupOrNull()

            val name = Q_PARAM_PATTERN.matchEntire(queryParams["q"]?.firstOrNull())?.groupOrNull()

            // Coordinates
            // https://waze.com/ul?ll={lat},{lon}
            (Regex("""ll\.$LAT,$LON""").matchEntire(queryParams["to"]?.firstOrNull())
                ?: LAT_LON_PATTERN.matchEntire(queryParams["ll"]?.firstOrNull())
                ?: LAT_LON_PATTERN.matchEntire(
                    queryParams[@Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "latlng"]?.firstOrNull()
                )
                )?.toLatLonPoint(Source.URI)?.let {
                    points = persistentListOf(WGS84Point(it, z, name))
                    return@run
                }

            // Search
            // https://waze.com/ul?q={name}
            if (name != null) {
                points = persistentListOf(WGS84Point(z = z, name = name, source = Source.URI))
            }

            // Place
            // https://ul.waze.com/ul?venue_id={id}
            queryParams["venue_id"]?.firstOrNull()?.takeIf { it.isNotEmpty() }?.let { venueId ->
                // To skip some redirects when downloading HTML, replace this URL:
                // https://ul.waze.com/ul?venue_id=2884104.28644432.6709020
                // or this URL:
                // https://www.waze.com/ul?venue_id=2884104.28644432.6709020
                // with this one:
                // https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020
                next = MatchedInput(
                    wazeHtmlInput.get(),
                    Uri(
                        scheme = "https",
                        host = "www.waze.com",
                        path = "/live-map/directions",
                        queryParams = listOf("to" to "place.w.$venueId").toQueryParams(),
                        uriQuote = uriQuote,
                    ).toString(),
                )
            } ?: queryParams["place"]?.firstOrNull()?.takeIf { it.isNotEmpty() }?.let { placeId ->
                // To skip some redirects when downloading HTML, replace this URL:
                // https://www.waze.com/live-map/directions?place=w.2884104.28644432.6709020
                // with this one:
                // https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020
                next = MatchedInput(
                    wazeHtmlInput.get(),
                    Uri(
                        scheme = "https",
                        host = "www.waze.com",
                        path = "/live-map/directions",
                        queryParams = listOf("to" to "place.$placeId").toQueryParams(),
                        uriQuote = uriQuote,
                    ).toString(),
                )
            } ?: queryParams["to"]?.firstOrNull()?.takeIf { it.startsWith("place.") }?.let {
                next = MatchedInput(wazeHtmlInput.get(), match)
            }
        }
    }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "https://waze.com/ul?ll={lat}%2C{lon}&z={z}")

    override fun toString() = "WazeUriInput"

    private companion object {
        private const val HASH =
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") """[0-9bcdefghjkmnpqrstuvwxyz]+"""
    }
}
