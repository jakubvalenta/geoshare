package page.ooooo.geoshare.lib.inputs

import androidx.compose.ui.res.stringResource
import com.google.re2j.Pattern
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.*
import page.ooooo.geoshare.lib.outputs.GeoUriOutput
import page.ooooo.geoshare.lib.point.*

object GeoUriInput : Input {
    private const val NAME_REGEX = """(\((?P<name>.+)\))"""

    override val uriPattern: Pattern =
        Pattern.compile("""geo:$LAT_NUM,$LON_NUM\?q=$LAT_NUM,\s*$LON_NUM|geo:\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.GEO_URI,
        nameResId = R.string.converter_geo_name,
        items = listOf(
            InputDocumentationItem.Text(3) {
                stringResource(
                    R.string.example,
                    GeoUriOutput.formatUriString(
                        persistentListOf(Point.example),
                        null,
                        nameDisabled = false,
                        zoomDisabled = false,
                    ),
                )
            },
        ),
    )

    override suspend fun parseUri(uri: Uri): ParseUriResult? =
        buildPoints {
            uri.run {
                ("""$LAT,$LON$NAME_REGEX?""" matchNaivePoint queryParams["q"])?.also { points.add(it) }
                    ?: (LAT_LON_PATTERN matchNaivePoint path)?.also { points.add(it) }

                queryParams
                    .filter { (key, value) -> key != "q" && key != "z" && value.isEmpty() }
                    .firstNotNullOfOrNull { (key) -> NAME_REGEX matchName key }
                    ?.also { defaultName = it }
                    ?: (Q_PARAM_PATTERN matchQ queryParams["q"])?.also { defaultName = it }
                
                (Z_PATTERN matchZ queryParams["z"])?.let { defaultZ = it }
            }
        }
            .asWGS84()
            .toParseUriResult()
}
