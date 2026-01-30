package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.extensions.matchQ
import page.ooooo.geoshare.lib.extensions.matchZ
import page.ooooo.geoshare.lib.extensions.toLat
import page.ooooo.geoshare.lib.extensions.toLon
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints
import page.ooooo.geoshare.lib.point.toParseUriResult

/**
 * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/
 */
object MagicEarthInput : Input {
    override val uriPattern: Pattern = Pattern.compile("""((https?://)?magicearth.com|magicearth:/)/\?\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.MAGIC_EARTH,
        nameResId = R.string.converter_magic_earth_name,
        items = listOf(
            InputDocumentationItem.Url(20, "https://magicearth.com/"),
        ),
    )

    override suspend fun parseUri(uri: Uri): ParseUriResult? =
        buildPoints {
            uri.run {
                (LAT_PATTERN match queryParams["lat"])?.toLat()?.let { lat ->
                    (LON_PATTERN match queryParams["lon"])?.toLon()?.let { lon ->
                        NaivePoint(lat, lon)
                    }
                }?.also { points.add(it) }

                @Suppress("SpellCheckingInspection")
                listOf("name", "daddr", "q")
                    .firstNotNullOfOrNull { key -> Q_PARAM_PATTERN matchQ queryParams[key] }
                    ?.also { defaultName = it }
                
                (Z_PATTERN matchZ queryParams["z"])?.also { defaultZ = it }
                    ?: (Z_PATTERN matchZ queryParams["zoom"])?.also { defaultZ = it }
            }
        }
            .asWGS84()
            .toParseUriResult()
}
