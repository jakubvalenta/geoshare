package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.data.formatters.UriFormatter
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.uri.Uri
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles Google Maps full URIs.
 *
 * It the URI is a Google Search URI, e.g. https://www.google.com/search..., it returns no points. The reason is that
 * Google Search URIs are handled by [GoogleSearchUriInput], which has higher priority in
 * [page.ooooo.geoshare.data.InputRepository], so Google Search URIs should never reach [GoogleMapsUriInput].
 */
@Singleton
class GoogleMapsUriInput @Inject constructor(
    private val googleMapsAddressApiInput: dagger.Lazy<GoogleMapsAddressApiInput>,
    private val googleMapsHtmlInput: dagger.Lazy<GoogleMapsHtmlInput>,
    private val googleMapsPlaceApiInput: dagger.Lazy<GoogleMapsPlaceApiInput>,
    private val googleMapsPlaceListInput: dagger.Lazy<GoogleMapsPlaceListInput>,
    private val uriQuote: UriQuote,
) : BasicOfflineInput, Input.HasPattern, Input.HasRandomSource {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = GoogleMapsInputGroup
    override val changelog = persistentListOf(
        InputChangelogItem.Url(5, "https://maps.google.com"),
        InputChangelogItem.Url(5, "https://google.com/maps"),
        InputChangelogItem.Url(5, "https://www.google.com/maps"),
    )

    override val pattern =
        Regex("""((?:https?://)?(?:(?:www|maps)\.)?google(?:\.[a-z]{2,3})?\.[a-z]{2,3}[/?#]$URI_REST)""")

    override fun parse(match: String, resources: Resources) =
        parseResult {
            val uri = Uri.parse(match, uriQuote)
            val googleMapsParseResult = GoogleMapsUriParser.parse(uri)
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

    override fun genRandomSource(point: Point) =
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
