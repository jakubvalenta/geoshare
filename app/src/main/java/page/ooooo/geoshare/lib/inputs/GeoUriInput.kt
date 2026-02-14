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
import page.ooooo.geoshare.lib.outputs.GeoUriOutput
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints

object GeoUriInput : Input {
    private const val NAME_REGEX = """\((.+)\)"""

    override val uriPattern =
        Regex("""geo:$LAT_NUM,$LON_NUM\?q=$LAT_NUM,\s*$LON_NUM|geo:\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.GEO_URI,
        nameResId = R.string.converter_geo_name,
        items = listOf(
            InputDocumentationItem.Text(3) {
                stringResource(
                    R.string.example,
                    GeoUriOutput.formatUriString(persistentListOf(Point.example), null),
                )
            },
        ),
    )

    override suspend fun parseUri(uri: Uri) = buildParseUriResult {
        points = buildPoints {
            uri.run {
                Regex("""$LAT,$LON(?:$NAME_REGEX)?""").matchEntire(queryParams["q"])
                    ?.toLatLonNamePoint()
                    ?.also { points.add(it) }
                    ?: LAT_LON_PATTERN.matchEntire(path)?.toLatLonPoint()?.also { points.add(it) }

                queryParams
                    .filter { (key, value) -> key != "q" && key != "z" && value.isEmpty() }
                    .firstNotNullOfOrNull { (key) -> Regex(NAME_REGEX).matchEntire(key)?.groupOrNull() }
                    ?.also { defaultName = it }
                    ?: Q_PARAM_PATTERN.matchEntire(queryParams["q"])?.groupOrNull()?.also { defaultName = it }

                Z_PATTERN.matchEntire(queryParams["z"])?.doubleGroupOrNull()?.also { defaultZ = it }
            }
        }.asWGS84()
    }
}
