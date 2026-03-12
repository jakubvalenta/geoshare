package page.ooooo.geoshare.lib.inputs

import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonNamePoint
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.formats.GeoUriFormat
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.WGS84Point

object GeoUriInput : Input, Input.HasRandomUri {
    private const val NAME_REGEX = """\((.+)\)"""

    override val uriPattern =
        Regex("""geo:$LAT_NUM,$LON_NUM\?q=$LAT_NUM,\s*$LON_NUM|geo:\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.GEO_URI,
        nameResId = R.string.converter_geo_name,
        items = listOf(
            InputDocumentationItem.Text(3) {
                stringResource(R.string.example, GeoUriFormat.formatGeoUriString(Point.example))
            },
        ),
    )

    override suspend fun parseUri(uri: Uri) = buildParseUriResult {
        uri.run {
            val z = Z_PATTERN.matchEntire(queryParams["z"])?.doubleGroupOrNull()

            val name = queryParams
                .filter { (key, value) -> key != "q" && key != "z" && value.isEmpty() }
                .firstNotNullOfOrNull { (key) -> Regex(NAME_REGEX).matchEntire(key)?.groupOrNull() }
                ?: Q_PARAM_PATTERN.matchEntire(queryParams["q"])?.groupOrNull()

            Regex("""$LAT,$LON(?:$NAME_REGEX)?""").matchEntire(queryParams["q"])?.toLatLonNamePoint()?.also {
                points = persistentListOf(it.asWGS84().copy(z = z, name = it.name ?: name))
                return@run
            }

            LAT_LON_PATTERN.matchEntire(path)?.toLatLonPoint()?.also {
                points = persistentListOf(it.asWGS84().copy(z = z, name = name))
                return@run
            }

            points = persistentListOf(WGS84Point(z = z, name = name))
        }
    }

    override fun genRandomUri(point: Point) =
        point.formatUriString("geo:{lat},{lon}?z={z}&q={lat},{lon}({name})")
}
