package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toZLatLonPoint
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.Source
import page.ooooo.geoshare.lib.point.WGS84Point

object OsmAndInput : Input, Input.HasRandomUri {
    override val uriPattern = Regex("""(?:https?://)?(?:www\.)?osmand\.net/$URI_REST""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.OSM_AND,
        nameResId = R.string.converter_osm_and_name,
        items = listOf(
            InputDocumentationItem.Url(20, "https://osmand.net/map"),
        ),
    )

    override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = buildParseUriResult {
        uri.run {
            val z = Regex("""$Z/.*""").matchEntire(fragment)?.doubleGroupOrNull()

            // Directions
            // https://osmand.net/map?start={lat},{lon}&finish={lat},{lon}
            LAT_LON_PATTERN.matchEntire(queryParams["finish"])?.toLatLonPoint(Source.URI).let { finish ->
                LAT_LON_PATTERN.matchEntire(queryParams["start"])?.toLatLonPoint(Source.URI).let { start ->
                    if (finish != null || start != null) {
                        points = listOfNotNull(start, finish)
                            .map { WGS84Point(it).copy(z = z) }
                            .toImmutableList()
                        return@run
                    }
                }
            }

            // Pin
            // https://osmand.net/map?pin={lat},{lon}
            LAT_LON_PATTERN.matchEntire(queryParams["pin"])?.toLatLonPoint(Source.URI)?.let {
                points = persistentListOf(WGS84Point(it).copy(z = z))
                return@run
            }

            // Map center
            // https://osmand.net/map#{z}/{lat}/{lon}
            Regex("""$Z/$LAT/$LON.*""").matchEntire(fragment)?.toZLatLonPoint(Source.MAP_CENTER)?.let {
                points = persistentListOf(WGS84Point(it).copy(z = z))
                return@run
            }
        }
    }

    override fun genRandomUri(point: Point) =
        point.formatUriString("https://osmand.net/map?pin={lat}%2C{lon}")
}
