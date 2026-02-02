package page.ooooo.geoshare.lib.inputs

import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints

/**
 * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/
 */
object MagicEarthInput : Input {
    override val uriPattern = Regex("""(?:(?:https?://)?magicearth.com|magicearth:/)/\?\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.MAGIC_EARTH,
        nameResId = R.string.converter_magic_earth_name,
        items = listOf(
            InputDocumentationItem.Url(20, "https://magicearth.com/"),
        ),
    )

    override suspend fun parseUri(uri: Uri) = buildParseUriResult {
        points = buildPoints {
            uri.run {
                LAT_PATTERN.matchEntire(queryParams["lat"])?.doubleGroupOrNull()?.let { lat ->
                    LON_PATTERN.matchEntire(queryParams["lon"])?.doubleGroupOrNull()?.let { lon ->
                        NaivePoint(lat, lon)
                    }
                }?.also { points.add(it) }

                @Suppress("SpellCheckingInspection")
                listOf("name", "daddr", "q")
                    .firstNotNullOfOrNull { key -> Q_PARAM_PATTERN.matchEntire(queryParams[key]) }
                    ?.groupOrNull()
                    ?.also { defaultName = it }

                Z_PATTERN.matchEntire(queryParams["z"])?.doubleGroupOrNull()?.also { defaultZ = it }
                    ?: Z_PATTERN.matchEntire(queryParams["zoom"])?.doubleGroupOrNull()?.also { defaultZ = it }
            }
        }.asWGS84()
    }
}
