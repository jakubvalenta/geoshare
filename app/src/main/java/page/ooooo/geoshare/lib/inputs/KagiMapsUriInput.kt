package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KagiMapsUriInput @Inject constructor(
    override val uriQuote: UriQuote,
) : UriInput, Input.HasRandomUri {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.KAGI_MAPS
    override val changelog = persistentListOf(
        InputChangelogItem.Url(51, "https://kagi.com/maps"),
    )

    override val pattern = Regex("""((?:https?://)?(?:www\.)?kagi\.com/maps/$URI_REST)""")

    override suspend fun parse(data: Uri, match: String, resources: Resources) = parseResult {
        data.run {
            // Coordinates

            // OSM id

            // Opaque id

            // Directions

            // Map center
        }
    }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "https://kagi.com/maps/info?q={name}&id=point_{lat}_{lon}&ll={lat}%2C{lon}#{z}/{lat}/{lon}")

    override fun toString() = "KagiMapsUriInput"
}
