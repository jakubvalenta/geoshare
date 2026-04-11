package page.ooooo.geoshare.lib.inputs

import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonNamePoint
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.formatters.GeoUriFormatter
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.Source
import page.ooooo.geoshare.lib.point.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeoUriInput @Inject constructor(
    private val geoUriFormatter: GeoUriFormatter,
    private val uriFormatter: UriFormatter,
) : Input, Input.HasRandomUri {
    override val uriPattern = Regex("""geo:$LAT_NUM,$LON_NUM\?q=$LAT_NUM,\s*$LON_NUM|geo:$URI_REST""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.GEO_URI,
        nameResId = R.string.converter_geo_name,
        items = listOf(
            InputDocumentationItem.Text(3) {
                stringResource(R.string.example, geoUriFormatter.formatGeoUriString(Point.example))
            },
        ),
    )

    override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = buildParseUriResult {
        uri.run {
            val z = Z_PATTERN.matchEntire(queryParams["z"])?.doubleGroupOrNull()

            // Name in separate param
            // ?q={lat},{lon}&({name})
            val name = queryParams
                .filter { (key, value) -> key != "q" && key != "z" && value.isEmpty() }
                .firstNotNullOfOrNull { (key) -> Regex(NAME_REGEX).matchEntire(key)?.groupOrNull() }
            // Query
            // ?q={name}
                ?: Q_PARAM_PATTERN.matchEntire(queryParams["q"])?.groupOrNull()

            // Pin without name
            // ?q={lat},{lon}
            // Pin with name
            // ?q={lat},{lon}({name})
            Regex("""$LAT,$LON(?:$NAME_REGEX)?""").matchEntire(queryParams["q"])?.toLatLonNamePoint(Source.URI)?.let {
                points = persistentListOf(WGS84Point(it).copy(z = z, name = it.name ?: name))
                return@run
            }

            // Coordinates
            // geo:{lat},{lon}
            LAT_LON_PATTERN.matchEntire(path)?.toLatLonPoint(Source.URI)?.let {
                points = persistentListOf(WGS84Point(it).copy(z = z, name = name))
                return@run
            }

            if (name != null) {
                points = persistentListOf(WGS84Point(z = z, name = name, source = Source.URI))
            }
        }
    }

    override fun genRandomUri(point: Point) =
        uriFormatter.formatUriString(point, "geo:{lat},{lon}?z={z}&q={lat},{lon}({name})")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is GeoUriInput
    }

    override fun hashCode() = javaClass.hashCode()

    private companion object {
        private const val NAME_REGEX = """\((.+)\)"""
    }
}
