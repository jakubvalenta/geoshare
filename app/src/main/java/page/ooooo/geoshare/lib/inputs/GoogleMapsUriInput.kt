package page.ooooo.geoshare.lib.inputs

import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Point
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles Google Maps full URIs.
 *
 * If the URI contains a Plus Code, e.g. https://www.google.com/maps/place/8FJ3HVHW%2B96, it doesn't process the code
 * but only puts it in the point name. The reason is that Plus Codes are handled by [PlusCodeInput], which has higher
 * priority in [page.ooooo.geoshare.data.InputRepository], so Google Maps URIs containing Plus Codes should never reach
 * [GoogleMapsUriInput].
 */
@Singleton
class GoogleMapsUriInput @Inject constructor(
    private val googleMapsAddressApiInput: dagger.Lazy<GoogleMapsAddressApiInput>,
    private val googleMapsHtmlInput: dagger.Lazy<GoogleMapsHtmlInput>,
    private val googleMapsPlaceApiInput: dagger.Lazy<GoogleMapsPlaceApiInput>,
    private val googleMapsPlaceListInput: dagger.Lazy<GoogleMapsPlaceListInput>,
    override val uriQuote: UriQuote,
) : UriInput, Input.HasRandomUri {
    override val pattern =
        Regex("""((?:https?://)?(?:(?:www|maps)\.)?google(?:\.[a-z]{2,3})?\.[a-z]{2,3}[/?#]$URI_REST)""")
    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.GOOGLE_MAPS,
        items = listOf(
            InputDocumentationItem.Url(5, "https://maps.google.com"),
            InputDocumentationItem.Url(5, "https://google.com/maps"),
            InputDocumentationItem.Url(5, "https://www.google.com/maps"),
        ),
    )

    override suspend fun parse(data: Uri, match: String) = parseResult {
        val googleMapsParseResult = GoogleMapsUriParser.parse(data)
        points = googleMapsParseResult.points
        next = if (googleMapsParseResult.isPlaceList) {
            MatchedInput(googleMapsPlaceListInput.get(), match)
        } else if (googleMapsParseResult.requiresHtmlParsing) {
            MatchedInput(googleMapsHtmlInput.get(), match)
        } else {
            val lastPoint = points.lastOrNull()
            if (lastPoint != null && !lastPoint.hasCoordinates()) {
                if (lastPoint.placeId != null) {
                    MatchedInput(googleMapsPlaceApiInput.get(), match)
                } else if (!lastPoint.name.isNullOrEmpty()) {
                    MatchedInput(googleMapsAddressApiInput.get(), match)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(
            point,
            listOf(
                "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
                "https://www.google.com/maps/dir/?api=1&destination={lat}%2C{lon}",
                "https://www.google.com/maps/@?api=1&map_action=pano&viewpoint={lat}%2C{lon}",
            ).random(),
        )

    override fun toString() = "GoogleMapsUriInput"
}
