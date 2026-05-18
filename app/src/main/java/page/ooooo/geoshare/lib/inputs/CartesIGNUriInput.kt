package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Singleton

@Singleton
class CartesIGNUriInput : UriInput, Input.HasRandomUri {
    override val pattern = Regex("""((?:https?://)?cartes-ign\.ign\.fr$URI_REST)""")
    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.CARTES_IGN,
        items = listOf(
            InputDocumentationItem.Url(39, "https://cartes-ign.ign.fr"),
        ),
    )

    override suspend fun parse(
        data: Uri,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        data.run {
            // Coordinates
            // https://cartes-ign.ign.fr?lng={lon}&lat={lat}&z={z}
            LAT_PATTERN.matchEntire(queryParams["lat"])?.doubleGroupOrNull()?.let { lat ->
                LON_PATTERN.matchEntire(queryParams["lng"])?.doubleGroupOrNull()?.let { lon ->
                    val z = Z_PATTERN.matchEntire(queryParams["z"])?.doubleGroupOrNull()
                    points = persistentListOf(WGS84Point(lat, lon, z, source = Source.URI))
                    return@run
                }
            }
        }
    }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "https://cartes-ign.ign.fr?lng={lon}&lat={lat}&z={z}")

    override fun toString() = "CartesIGNUriInput"
}
