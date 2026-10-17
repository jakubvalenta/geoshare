package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.extensions.groupOrNull
import page.ooooo.geoshare.data.extensions.matchEntire
import page.ooooo.geoshare.data.extensions.toLatLonPoint
import page.ooooo.geoshare.data.formatters.GoogleMapsUriFormatter
import page.ooooo.geoshare.data.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.data.geo.NaivePoint
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.uri.Uri
import page.ooooo.geoshare.data.uri.UriQuote
import page.ooooo.geoshare.data.uri.toQueryParams
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles URIs with the `google.navigation:` scheme.
 *
 * Assumes the coordinates are in the [GCJ02MainlandChinaPoint] coordinate system, but we really don't know, it depends
 * on the app or website that created the URI.
 *
 * See https://developer.android.com/guide/components/google-maps-intents#launch-turn-by-turn-navigation
 */
@Singleton
class GoogleNavigationUriInput @Inject constructor(
    private val googleMapsAddressApiInput: dagger.Lazy<GoogleMapsAddressApiInput>,
    private val uriQuote: UriQuote,
) : BasicOfflineInput, Input.HasPattern, Input.HasRandomUri {
    override fun getName(resources: Resources) = group.getName(resources)
    override val group = InputGroup.GOOGLE_NAVIGATION_URI
    override val changelog = persistentListOf(
        InputChangelogItem.Text(45) {
            stringResource(
                R.string.example,
                GoogleMapsUriFormatter.formatNavigationUriString(
                    GCJ02MainlandChinaPoint(NaivePoint.example), uriQuote
                )
            )
        },
    )

    override val pattern = Regex("""(google.navigation:$URI_REST)""")

    override fun parse(match: String, resources: Resources) =
        Uri.parse(match, uriQuote).run {
            parseResult {
                val q = Regex("""(?:^|.*&)q=([^&]+).*""").matchEntire(pathParts.firstOrNull())?.groupOrNull()

                // Coordinates
                // google.navigation:q={lat},{lon}
                LAT_LON_PATTERN.matchEntire(q)?.toLatLonPoint(Source.URI)?.let {
                    points = persistentListOf(GCJ02MainlandChinaPoint(it))
                    return@parseResult
                }

                // Search
                // google.navigation:q={query}
                Q_PATH_PATTERN.matchEntire(q)?.groupOrNull()?.let {
                    points = persistentListOf(GCJ02MainlandChinaPoint(name = it, source = Source.URI))
                    // Go to API parsing
                    next = MatchedInput(
                        googleMapsAddressApiInput.get(),
                        Uri(
                            scheme = "https",
                            host = "maps.google.com",
                            queryParams = listOf("q" to it).toQueryParams(),
                            uriQuote = uriQuote,
                        ).toString()
                    )
                    return@parseResult
                }
            }
        }

    override fun genRandomUri(point: Point) =
        GoogleMapsUriFormatter.formatNavigationUriString(point, uriQuote)

    override fun toString() = "GoogleNavigationUriInput"
}
