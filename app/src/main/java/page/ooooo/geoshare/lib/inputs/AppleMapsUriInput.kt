package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppleMapsUriInput @Inject constructor(
    private val appleMapsHtmlInput: dagger.Lazy<AppleMapsHtmlInput>,
    override val uriQuote: UriQuote,
) : UriInput, Input.HasRandomUri {
    override val pattern = Regex("""((?:https?://)?maps\.apple(\.com)?[/?#]$URI_REST)""")
    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.APPLE_MAPS,
        items = listOf(
            InputDocumentationItem.Url(18, "https://maps.apple"),
            InputDocumentationItem.Url(18, "https://maps.apple.com"),
        ),
    )

    override suspend fun parse(data: Uri, match: String, prevResult: ParseResult?) = buildParseResult {
        data.run {
            val z = Z_PATTERN.matchEntire(queryParams["z"])?.doubleGroupOrNull()

            // Search or place with name
            // https://maps.apple.com/?q={name}
            // https://maps.apple.com/place?place-id={id}...&q={name}
            val name = listOf("name", "address", @Suppress("SpellCheckingInspection") "daddr", "q")
                .firstNotNullOfOrNull { key -> Q_PARAM_PATTERN.matchEntire(queryParams[key])?.groupOrNull() }

            // Coordinates
            // https://maps.apple.com/?ll={lat},{lon}
            listOf("ll", @Suppress("SpellCheckingInspection") "daddr", "coordinate", "q")
                .firstNotNullOfOrNull { key ->
                    LAT_LON_PATTERN.matchEntire(queryParams[key])?.toLatLonPoint(Source.URI)
                }?.let {
                    points = persistentListOf(WGS84Point(it).copy(z = z, name = name))
                    return@run
                }

            // Map center (including the search center 'sll')
            // https://maps.apple.com/?center={lat},{lon}
            listOf("sll", "center")
                .firstNotNullOfOrNull { key ->
                    LAT_LON_PATTERN.matchEntire(queryParams[key])?.toLatLonPoint(Source.MAP_CENTER)
                }?.let {
                    points = persistentListOf(WGS84Point(it).copy(z = z, name = name))
                    return@run
                }

            // Short link
            // https://maps.apple/p/{hash}
            if (host == "maps.apple" && pathParts.firstOrNull() == "" && pathParts.getOrNull(1) == "p" ||
                // Place
                // https://maps.apple.com/place?auid={id}...
                !queryParams[@Suppress("SpellCheckingInspection") "auid"].isNullOrEmpty() ||
                // Place
                // https://maps.apple.com/place?place-id={id}...
                !queryParams["place-id"].isNullOrEmpty()
            ) {
                nextStep = NextStep(appleMapsHtmlInput.get(), match)
            }

            if (name != null) {
                points = persistentListOf(WGS84Point(z = z, name = name, source = Source.URI))
            }
        }
    }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(
            point,
            listOf(
                "https://maps.apple.com/?ll={lat}%2C{lon}&z={z}&q={name}",
                "https://maps.apple.com/?daddr={lat}%2C{lon}",
            ).random(),
        )

    override fun toString() = "AppleMapsUriInput"
}
