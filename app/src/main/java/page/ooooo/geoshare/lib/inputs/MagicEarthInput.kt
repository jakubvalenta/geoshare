package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.WGS84Point

/**
 * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/
 */
object MagicEarthInput : Input, Input.HasRandomUri {
    override val uriPattern = Regex("""(?:(?:https?://)?magicearth.com|magicearth:/)/\?\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.MAGIC_EARTH,
        nameResId = R.string.converter_magic_earth_name,
        items = listOf(
            InputDocumentationItem.Url(20, "https://magicearth.com/"),
        ),
    )

    override suspend fun parseUri(uri: Uri) = buildParseUriResult {
        uri.run {
            val z = listOf("z", "zoom")
                .firstNotNullOfOrNull { key -> Z_PATTERN.matchEntire(queryParams[key])?.doubleGroupOrNull() }

            val name = listOf("name", @Suppress("SpellCheckingInspection") "daddr", "q")
                .firstNotNullOfOrNull { key -> Q_PARAM_PATTERN.matchEntire(queryParams[key])?.groupOrNull() }

            LAT_PATTERN.matchEntire(queryParams["lat"])?.doubleGroupOrNull()?.let { lat ->
                LON_PATTERN.matchEntire(queryParams["lon"])?.doubleGroupOrNull()?.let { lon ->
                    points = persistentListOf(WGS84Point(lat, lon, z, name))
                    return@run
                }
            }

            points = persistentListOf(WGS84Point(z = z, name = name))
        }
    }

    override fun genRandomUri(point: Point) =
        point.formatUriString("https://magicearth.com/?show_on_map&lat={lat}&lon={lon}&name={name}&z={z}")
}
