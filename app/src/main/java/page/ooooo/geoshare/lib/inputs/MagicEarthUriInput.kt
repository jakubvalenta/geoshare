package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.network.FetchTools
import javax.inject.Inject
import javax.inject.Singleton

/**
 * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/
 */
@Singleton
class MagicEarthUriInput @Inject constructor(
    val uriQuote: UriQuote,
) : BasicInput, Input.HasPattern, Input.HasRandomUri {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.MAGIC_EARTH
    override val changelog = persistentListOf(
        InputChangelogItem.Url(20, "https://magicearth.com/"),
    )

    override val pattern = Regex("""((?:(?:https?://)?magicearth.com|magicearth:/)/\?$URI_REST)""")

    override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
        Uri.parse(match, uriQuote).run {
            parseResult {
                val z = listOf("z", "zoom")
                    .firstNotNullOfOrNull { key ->
                        Z_PATTERN.matchEntire(queryParams[key]?.firstOrNull())?.doubleGroupOrNull()
                    }

                val name = listOf("name", @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "daddr", "q")
                    .firstNotNullOfOrNull { key ->
                        Q_PARAM_PATTERN.matchEntire(queryParams[key]?.firstOrNull())?.groupOrNull()
                    }

                LAT_PATTERN.matchEntire(queryParams["lat"]?.firstOrNull())?.doubleGroupOrNull()?.let { lat ->
                    LON_PATTERN.matchEntire(queryParams["lon"]?.firstOrNull())?.doubleGroupOrNull()?.let { lon ->
                        points = persistentListOf(WGS84Point(lat, lon, z, name, source = Source.URI))
                        return@parseResult
                    }
                }

                if (name != null) {
                    points = persistentListOf(WGS84Point(z = z, name = name, source = Source.URI))
                }
            }
        }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(
            point,
            "https://magicearth.com/?show_on_map&lat={lat}&lon={lon}&name={name}&z={z}",
        )

    override fun toString() = "MagicEarthUriInput"
}
