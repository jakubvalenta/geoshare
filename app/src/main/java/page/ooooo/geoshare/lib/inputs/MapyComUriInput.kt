package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.geo.decodeMapyComGeoHash
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapyComUriInput @Inject constructor(
    override val uriQuote: UriQuote,
) : UriInput, Input.HasRandomUri {
    override val group = InputGroup.MAPY_COM
    override val changelog = persistentListOf(
        InputChangelogItem.Url(23, "https://mapy.com"),
        InputChangelogItem.Url(23, "https://mapy.cz"),
        InputChangelogItem.Url(23, "https://www.mapy.com"),
        InputChangelogItem.Url(23, "https://www.mapy.cz"),
    )
    override val pattern = Regex("""($COORDS|(?:https?://)?(?:(?:hapticke|www)\.)?mapy\.[a-z]{2,3}[/?]$URI_REST)""")

    override suspend fun parse(data: Uri, match: String, resources: Resources) = parseResult {
        data.run {
            val z = Z_PATTERN.matchEntire(queryParams["z"])?.doubleGroupOrNull()

            // Navigation
            // https://mapy.com/...?rc={hash}
            queryParams["rc"].takeIf { !it.isNullOrEmpty() }?.let { hash ->
                points = decodeMapyComGeoHash(hash).map { WGS84Point(it, z) }.toImmutableList()
                return@run
            }

            // Point with id
            // https://mapy.com/...?id={lon}%2C{lat}
            LON_LAT_PATTERN.matchEntire(queryParams["id"])?.toLonLatPoint(Source.URI)?.let {
                points = persistentListOf(WGS84Point(it, z))
                return@run
            }

            // Coordinates in text -- use them, because they're more precise than the URL
            // e.g. `Vega de Tera 41.9966006N, 6.1223825W https://mapy.com/s/{id}`
            Regex(COORDS).matchEntire(pathParts.firstOrNull())?.let { m ->
                m.groupValues[0].let { entireMatch ->
                    m.doubleGroupOrNull(1)?.let { lat ->
                        m.doubleGroupOrNull(2)?.let { lon ->
                            val latSig = if (entireMatch.contains('S')) -1 else 1
                            val lonSig = if (entireMatch.contains('W')) -1 else 1
                            points = persistentListOf(WGS84Point(latSig * lat, lonSig * lon, z, source = Source.TEXT))
                            return@run
                        }
                    }
                }
            }

            // Coordinates in URL
            // https://mapy.com/...?x={lon}&y={lat}&z={z}
            LAT_PATTERN.matchEntire(queryParams["y"])?.doubleGroupOrNull()?.let { lat ->
                LON_PATTERN.matchEntire(queryParams["x"])?.doubleGroupOrNull()?.let { lon ->
                    points = persistentListOf(WGS84Point(lat, lon, z, source = Source.MAP_CENTER))
                    return@run
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
