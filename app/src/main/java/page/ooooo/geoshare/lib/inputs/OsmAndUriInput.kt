package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toZLatLonPoint
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OsmAndUriInput @Inject constructor() : UriInput, Input.HasRandomUri {
    override val pattern = Regex("""((?:https?://)?(?:www\.)?osmand\.net/$URI_REST)""")
    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.OSM_AND,
        items = listOf(
            InputDocumentationItem.Url(20, "https://osmand.net/map"),
        ),
    )

    override suspend fun parse(
        data: Uri,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        data.run {
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
        UriFormatter.formatUriString(point, "https://osmand.net/map?pin={lat}%2C{lon}")

    override fun toString() = "OsmAndUriInput"
}
