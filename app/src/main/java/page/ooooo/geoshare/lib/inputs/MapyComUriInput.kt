package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapyComUriInput @Inject constructor(
    override val uriQuote: UriQuote,
) : UriInput, Input.HasRandomUri {
    override val pattern = Regex("""($COORDS|(?:https?://)?(?:(?:hapticke|www)\.)?mapy\.[a-z]{2,3}[/?]$URI_REST)""")
    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.MAPY_COM,
        items = listOf(
            InputDocumentationItem.Url(23, "https://mapy.com"),
            InputDocumentationItem.Url(23, "https://mapy.cz"),
            InputDocumentationItem.Url(23, "https://www.mapy.com"),
            InputDocumentationItem.Url(23, "https://www.mapy.cz"),
        ),
    )

    override suspend fun parse(data: Uri, match: String) = parseResult {
        data.run {
            // Coordinates -- use this part of the text, because it's more precise than the URL
            // e.g. `Vega de Tera 41.9966006N, 6.1223825W https://mapy.com/s/deduduzeha`
            Regex(COORDS).matchEntire(pathParts.firstOrNull())?.let { m ->
                m.groupValues[0].let { entireMatch ->
                    m.doubleGroupOrNull(1)?.let { lat ->
                        m.doubleGroupOrNull(2)?.let { lon ->
                            val latSig = if (entireMatch.contains('S')) -1 else 1
                            val lonSig = if (entireMatch.contains('W')) -1 else 1
                            points = persistentListOf(WGS84Point(latSig * lat, lonSig * lon, source = Source.TEXT))
                            return@run
                        }
                    }
                }
            }

            // Query params
            // https://mapy.com/...?x={lon}&y={lat}&z={z}
            LAT_PATTERN.matchEntire(queryParams["y"])?.doubleGroupOrNull()?.let { lat ->
                LON_PATTERN.matchEntire(queryParams["x"])?.doubleGroupOrNull()?.let { lon ->
                    Z_PATTERN.matchEntire(queryParams["z"])?.doubleGroupOrNull().let { z ->
                        points = persistentListOf(WGS84Point(lat, lon, z, source = Source.URI))
                        return@run
                    }
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
