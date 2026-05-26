package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
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
import javax.inject.Inject
import javax.inject.Singleton

/**
 * See https://developers.google.com/waze/deeplinks/
 */
@Singleton
class WazeUriInput @Inject constructor(
    private val wazeHtmlInput: dagger.Lazy<WazeHtmlInput>,
    override val uriQuote: UriQuote,
) : UriInput, Input.HasRandomUri {
    override val pattern = Regex("""((?:https?://)?(?:(?:www|ul)\.)?waze\.com/$URI_REST)""")

    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.WAZE,
        items = listOf(
            InputDocumentationItem.Url(21, "https://waze.com/live-map"),
            InputDocumentationItem.Url(21, "https://waze.com/ul"),
            InputDocumentationItem.Url(21, "https://www.waze.com/live-map"),
            InputDocumentationItem.Url(21, "https://www.waze.com/ul"),
            InputDocumentationItem.Url(21, "https://ul.waze.com/ul"),
        ),
    )

    override suspend fun parse(data: Uri, match: String) = parseResult {
        data.run {
            // Short link
            // https://waze.com/ul/h{hash}
            (if (pathParts.firstOrNull() == "" && pathParts.getOrNull(1) == "ul") {
                Regex("""h($HASH)""").matchEntire(pathParts.getOrNull(2))
            } else {
                null
            }
            // https://www.waze.com/live-map?h={hash}
                ?: Regex("($HASH)").matchEntire(queryParams["h"])
                )?.groupOrNull()
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

            val z = Z_PATTERN.matchEntire(queryParams["z"])?.doubleGroupOrNull()

            val name = Q_PARAM_PATTERN.matchEntire(queryParams["q"])?.groupOrNull()

            // Coordinates
            // https://waze.com/ul?ll={lat},{lon}
            (Regex("""ll\.$LAT,$LON""").matchEntire(queryParams["to"])
                ?: LAT_LON_PATTERN.matchEntire(queryParams["ll"])
                ?: LAT_LON_PATTERN.matchEntire(queryParams[@Suppress("SpellCheckingInspection") "latlng"])
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
            queryParams["venue_id"]?.takeIf { it.isNotEmpty() }?.let { venueId ->
                // To skip some redirects when downloading HTML, replace this URL:
                // https://ul.waze.com/ul?venue_id=2884104.28644432.6709020
                // or this URL:
                // https://www.waze.com/ul?venue_id=2884104.28644432.6709020
                // with this one:
                // https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020
                nextStep = NextStep(
                    wazeHtmlInput.get(),
                    Uri(
                        scheme = "https",
                        host = "www.waze.com",
                        path = "/live-map/directions",
                        queryParams = persistentMapOf("to" to "place.w.$venueId"),
                        uriQuote = uriQuote,
                    ).toString(),
                )
            } ?: queryParams["place"]?.takeIf { it.isNotEmpty() }?.let { placeId ->
                // To skip some redirects when downloading HTML, replace this URL:
                // https://www.waze.com/live-map/directions?place=w.2884104.28644432.6709020
                // with this one:
                // https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020
                nextStep = NextStep(
                    wazeHtmlInput.get(),
                    Uri(
                        scheme = "https",
                        host = "www.waze.com",
                        path = "/live-map/directions",
                        queryParams = persistentMapOf("to" to "place.$placeId"),
                        uriQuote = uriQuote,
                    ).toString(),
                )
            } ?: queryParams["to"]?.takeIf { it.startsWith("place.") }?.let {
                nextStep = NextStep(wazeHtmlInput.get(), match)
            }
        }
    }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "https://waze.com/ul?ll={lat}%2C{lon}&z={z}")

    override fun toString() = "WazeUriInput"

    private companion object {
        private const val HASH = @Suppress("SpellCheckingInspection") """[0-9bcdefghjkmnpqrstuvwxyz]+"""
    }
}
