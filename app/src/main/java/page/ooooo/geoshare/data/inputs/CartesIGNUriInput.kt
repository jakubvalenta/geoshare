package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.data.extensions.doubleGroupOrNull
import page.ooooo.geoshare.data.extensions.matchEntire
import page.ooooo.geoshare.data.formatters.UriFormatter
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.uri.Uri
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartesIGNUriInput @Inject constructor(
    val uriQuote: UriQuote,
) : BasicOfflineInput, Input.HasPattern, Input.HasRandomUri {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.CARTES_IGN
    override val changelog = persistentListOf(
        InputChangelogItem.Url(39, "https://cartes-ign.ign.fr"),
    )

    override val pattern = Regex("""((?:https?://)?cartes-ign\.ign\.fr$URI_REST)""")

    override fun parse(match: String, resources: Resources): ParseResult =
        Uri.parse(match, uriQuote).run {
            parseResult {
                // Coordinates
                // https://cartes-ign.ign.fr?lng={lon}&lat={lat}&z={z}
                LAT_PATTERN.matchEntire(queryParams["lat"]?.firstOrNull())?.doubleGroupOrNull()?.let { lat ->
                    LON_PATTERN.matchEntire(queryParams["lng"]?.firstOrNull())?.doubleGroupOrNull()?.let { lon ->
                        val z = Z_PATTERN.matchEntire(queryParams["z"]?.firstOrNull())?.doubleGroupOrNull()
                        points = persistentListOf(WGS84Point(lat, lon, z, source = Source.URI))
                        return@parseResult
                    }
                }
            }
        }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "https://cartes-ign.ign.fr?lng={lon}&lat={lat}&z={z}")

    override fun toString() = "CartesIGNUriInput"
}
