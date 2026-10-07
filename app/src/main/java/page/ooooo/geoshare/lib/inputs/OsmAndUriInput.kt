package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toZLatLonPoint
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.network.FetchTools
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OsmAndUriInput @Inject constructor(
    val uriQuote: UriQuote,
) : BasicInput, Input.HasPattern, Input.HasRandomUri {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.OSM_AND
    override val changelog = persistentListOf(
        InputChangelogItem.Url(20, "https://osmand.net/map"),
    )

    override val pattern = Regex("""((?:https?://)?(?:www\.)?osmand\.net/$URI_REST)""")

    override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
        Uri.parse(match, uriQuote).run {
            parseResult {
                val z = Regex("""$Z/.*""").matchEntire(fragment)?.doubleGroupOrNull()

                // Directions
                // https://osmand.net/map?start={lat},{lon}&finish={lat},{lon}
                LAT_LON_PATTERN.matchEntire(queryParams["finish"]?.firstOrNull())?.toLatLonPoint(Source.URI)
                    .let { finish ->
                        LAT_LON_PATTERN.matchEntire(queryParams["start"]?.firstOrNull())?.toLatLonPoint(Source.URI)
                            .let { start ->
                                if (finish != null || start != null) {
                                    points = listOfNotNull(start, finish)
                                        .map { WGS84Point(it, z) }
                                        .toImmutableList()
                                    return@parseResult
                                }
                            }
                    }

                // Pin
                // https://osmand.net/map?pin={lat},{lon}
                LAT_LON_PATTERN.matchEntire(queryParams["pin"]?.firstOrNull())?.toLatLonPoint(Source.URI)?.let {
                    points = persistentListOf(WGS84Point(it, z))
                    return@parseResult
                }

                // Map center
                // https://osmand.net/map#{z}/{lat}/{lon}
                Regex("""$Z/$LAT/$LON.*""").matchEntire(fragment)?.toZLatLonPoint(Source.MAP_CENTER)?.let {
                    points = persistentListOf(WGS84Point(it, z))
                    return@parseResult
                }
            }
        }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "https://osmand.net/map?pin={lat}%2C{lon}")

    override fun toString() = "OsmAndUriInput"
}
