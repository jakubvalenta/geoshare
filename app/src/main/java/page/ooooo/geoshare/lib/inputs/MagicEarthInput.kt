package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

/**
 * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/
 */
@Singleton
class MagicEarthInput @Inject constructor(
    private val uriFormatter: UriFormatter,
) : Input, Input.HasRandomUri {
    override val uriPattern = Regex("""(?:(?:https?://)?magicearth.com|magicearth:/)/\?$URI_REST""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.MAGIC_EARTH,
        nameResId = R.string.converter_magic_earth_name,
        items = listOf(
            InputDocumentationItem.Url(20, "https://magicearth.com/"),
        ),
    )

    override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = buildParseUriResult {
        uri.run {
            val z = listOf("z", "zoom")
                .firstNotNullOfOrNull { key -> Z_PATTERN.matchEntire(queryParams[key])?.doubleGroupOrNull() }

            val name = listOf("name", @Suppress("SpellCheckingInspection") "daddr", "q")
                .firstNotNullOfOrNull { key -> Q_PARAM_PATTERN.matchEntire(queryParams[key])?.groupOrNull() }

            LAT_PATTERN.matchEntire(queryParams["lat"])?.doubleGroupOrNull()?.let { lat ->
                LON_PATTERN.matchEntire(queryParams["lon"])?.doubleGroupOrNull()?.let { lon ->
                    points = persistentListOf(WGS84Point(lat, lon, z, name, Source.URI))
                    return@run
                }
            }

            if (name != null) {
                points = persistentListOf(WGS84Point(z = z, name = name, source = Source.URI))
            }
        }
    }

    override fun genRandomUri(point: Point) =
        uriFormatter.formatUriString(
            point,
            "https://magicearth.com/?show_on_map&lat={lat}&lon={lon}&name={name}&z={z}",
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is MagicEarthInput
    }

    override fun hashCode() = javaClass.hashCode()
}
