package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

object CartesIGNInput : Input, Input.HasRandomUri {
    override val uriPattern = Regex("""((?:https?://)?cartes-ign\.ign\.fr$URI_REST)""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.CARTES_IGN,
        nameResId = R.string.converter_cartes_ign_name,
        items = listOf(
            InputDocumentationItem.Url(39, "https://cartes-ign.ign.fr"),
        ),
    )

    override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = buildParseUriResult {
        uri.run {
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
}
