package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.geo.decodeMapyComGeoHash
import page.ooooo.geoshare.lib.network.FetchTools
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapyComUriInput @Inject constructor(
    val uriQuote: UriQuote,
) : BasicInput, Input.HasPattern, Input.HasRandomUri {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.MAPY_COM
    override val changelog = persistentListOf(
        InputChangelogItem.Url(23, "https://mapy.com"),
        InputChangelogItem.Url(23, "https://mapy.cz"),
        InputChangelogItem.Url(23, "https://www.mapy.com"),
        InputChangelogItem.Url(23, "https://www.mapy.cz"),
    )

    override val pattern = Regex("""($COORDS|(?:https?://)?(?:(?:hapticke|www)\.)?mapy\.[a-z]{2,3}[/?]$URI_REST)""")

    override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
        Uri.parse(match, uriQuote).run {
            parseResult {
                val z = Z_PATTERN.matchEntire(queryParams["z"]?.firstOrNull())?.doubleGroupOrNull()

                // Navigation
                // https://mapy.com/...?rc={hash}
                queryParams["rc"]?.firstOrNull().takeIf { !it.isNullOrEmpty() }?.let { hash ->
                    points = decodeMapyComGeoHash(hash).map { WGS84Point(it, z) }.toImmutableList()
                    return@parseResult
                }

                // Point with id
                // https://mapy.com/...?id={lon}%2C{lat}
                LON_LAT_PATTERN.matchEntire(queryParams["id"]?.firstOrNull())?.toLonLatPoint(Source.URI)?.let {
                    points = persistentListOf(WGS84Point(it, z))
                    return@parseResult
                }

                // Coordinates in text -- use them, because they're more precise than the URL
                // e.g. `Vega de Tera 41.9966006N, 6.1223825W https://mapy.com/s/{id}`
                Regex(COORDS).matchEntire(pathParts.firstOrNull())?.let { m ->
                    m.groupValues[0].let { entireMatch ->
                        m.doubleGroupOrNull(1)?.let { lat ->
                            m.doubleGroupOrNull(2)?.let { lon ->
                                val latSig = if (entireMatch.contains('S')) -1 else 1
                                val lonSig = if (entireMatch.contains('W')) -1 else 1
                                points =
                                    persistentListOf(WGS84Point(latSig * lat, lonSig * lon, z, source = Source.TEXT))
                                return@parseResult
                            }
                        }
                    }
                }

                // Coordinates in URL
                // https://mapy.com/...?x={lon}&y={lat}&z={z}
                LAT_PATTERN.matchEntire(queryParams["y"]?.firstOrNull())?.doubleGroupOrNull()?.let { lat ->
                    LON_PATTERN.matchEntire(queryParams["x"]?.firstOrNull())?.doubleGroupOrNull()?.let { lon ->
                        points = persistentListOf(WGS84Point(lat, lon, z, source = Source.MAP_CENTER))
                        return@parseResult
                    }
                }
            }
        }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "https://mapy.com/en/zakladni?x={lon}&y={lat}&z={z}")

    override fun toString() = "MapsComUriInput"

    private companion object {
        private const val COORDS = """(\d{1,2}(?:\.\d{1,16})?)[NS], (\d{1,3}(?:\.\d{1,16})?)[WE]"""
    }
}
