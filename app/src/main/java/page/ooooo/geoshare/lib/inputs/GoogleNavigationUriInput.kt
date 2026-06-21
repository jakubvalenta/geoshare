package page.ooooo.geoshare.lib.inputs

import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.formatters.GoogleMapsUriFormatter
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

/**
 * See https://developer.android.com/guide/components/google-maps-intents#launch-turn-by-turn-navigation
 */
@Singleton
class GoogleNavigationUriInput @Inject constructor(
    private val googleMapsAddressApiInput: dagger.Lazy<GoogleMapsAddressApiInput>,
    override val uriQuote: UriQuote,
) : UriInput, Input.HasRandomUri {
    override val pattern = Regex("""(google.navigation:$URI_REST)""")
    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.GOOGLE_NAVIGATION_URI,
        items = listOf(
            InputDocumentationItem.Text(45) {
                stringResource(
                    R.string.example,
                    GoogleMapsUriFormatter.formatNavigationUriString(WGS84Point(NaivePoint.example), uriQuote)
                )
            },
        ),
    )

    override suspend fun parse(data: Uri, match: String) = parseResult {
        data.run {
            val q = Regex("""(?:^|.*&)q=([^&]+).*""").matchEntire(pathParts.firstOrNull())?.groupOrNull()

            // Coordinates
            // google.navigation:q={lat},{lon}
            LAT_LON_PATTERN.matchEntire(q)?.toLatLonPoint(Source.URI)?.let {
                points = persistentListOf(WGS84Point(it)) // TODO What about China?
                return@run
            }

            // Search
            // google.navigation:q={query}
            Q_PATH_PATTERN.matchEntire(q)?.groupOrNull()?.let {
                points = persistentListOf(WGS84Point(name = it, source = Source.URI))
                // Go to API parsing
                next = MatchedInput(
                    googleMapsAddressApiInput.get(),
                    Uri(
                        scheme = "https",
                        host = "maps.google.com",
                        queryParams = mapOf("q" to it).toImmutableMap(),
                        uriQuote = uriQuote,
                    ).toString()
                )
                return@run
            }
        }
    }

    override fun genRandomUri(point: Point) =
        GoogleMapsUriFormatter.formatNavigationUriString(point, uriQuote)

    override fun toString() = "GoogleNavigationUriInput"
}
