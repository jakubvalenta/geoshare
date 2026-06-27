package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.client.engine.HttpClientEngine
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.decodeBasicHtmlEntities
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Source
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsHtmlInputImpl @Inject constructor(
    private val googleMapsUriInput: dagger.Lazy<GoogleMapsUriInput>,
    private val googleMapsWebViewInput: dagger.Lazy<GoogleMapsWebViewInput>,
    override val log: Log,
    override val engine: HttpClientEngine,
    override val uriQuote: UriQuote,
) : GoogleMapsHtmlInput, BodyAsChannelInput {
    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title

    override val cookies = GoogleMapsShortLinkInput.COOKIES
    override val userAgent = GoogleMapsShortLinkInput.USER_AGENT

    override suspend fun parse(data: ByteReadChannel, match: String) = parseResult {
        val directionsPreviewPattern = Regex("""%213d$LAT%214d$LON""")
        val pointPattern = Regex("""\[(?:null,null,|null,\[)$LAT,$LON]""")
        val defaultPointLinkPattern = Regex("""/@$LAT,$LON""")
        val defaultPointAppInitStatePattern =
            Regex("""APP_INITIALIZATION_STATE=\[\[\[[\d.-]+,$LON,$LAT""")
        val genericMetaTagPattern = Regex("""<meta content="Google Maps" itemprop="name"""")
        val uriPattern = Regex("""data-url="([^"]+)"""")

        val mutableNaivePoints = mutableListOf<NaivePoint>()
        var defaultNaivePoint: NaivePoint? = null
        var genericMetaTagFound = false
        var redirectUriString: String? = null

        while (true) {
            val line = data.readLine() ?: break
            if (!genericMetaTagFound && genericMetaTagPattern.find(line) != null) {
                log.d(TAG, "Generic meta tag matched line $line")
                genericMetaTagFound = true
            }
            if (
                mutableNaivePoints.addAll(
                    directionsPreviewPattern.findAll(line)
                        .mapNotNull { m -> m.toLatLonPoint(Source.HTML) }
                )
            ) {
                log.d(TAG, "Directions preview pattern matched line $line")
                // Stop parsing, so that we don't add points that we've just parsed again from SCRIPT tags
                break
            }
            if (
                mutableNaivePoints.addAll(
                    pointPattern.findAll(line)
                        .mapNotNull { m -> m.toLatLonPoint(Source.JAVASCRIPT) }
                )
            ) {
                log.d(TAG, "Point pattern matched line $line")
            }
            if (defaultNaivePoint == null) {
                defaultPointLinkPattern.find(line)?.toLatLonPoint(Source.JAVASCRIPT)?.let {
                    log.d(TAG, "Default point pattern 1 matched line $line")
                    defaultNaivePoint = it
                }
            }
            if (defaultNaivePoint == null && !genericMetaTagFound) {
                // When the HTML contains a generic "Google Maps" META tag instead of a specific one like
                // "Berlin - Germany", then it seems that the APP_INITIALIZATION_STATE doesn't contain correct
                // coordinates. It contains coordinates of the IP address that the HTTP request came from. So let's
                // ignore these coordinates.
                defaultPointAppInitStatePattern.find(line)?.toLonLatPoint(Source.JAVASCRIPT)?.let {
                    log.d(TAG, "Default point pattern 2 matched line $line")
                    defaultNaivePoint = it
                }
            }
            if (redirectUriString == null) {
                uriPattern.find(line)?.groupOrNull()?.let { attr ->
                    log.d(TAG, "URI pattern matched line $line")
                    redirectUriString = attr.decodeBasicHtmlEntities()
                }
            }
        }

        if (mutableNaivePoints.isEmpty()) {
            if (defaultNaivePoint != null) {
                mutableNaivePoints.add(defaultNaivePoint)
            } else if (redirectUriString != null) {
                val baseUri = Uri.parse(match, uriQuote)
                val redirectUri = Uri.parse(redirectUriString, uriQuote).toAbsoluteUri(baseUri)
                next = MatchedInput(googleMapsUriInput.get(), redirectUri.toString())
            } else {
                // Go to web parsing
                next = MatchedInput(googleMapsWebViewInput.get(), match)
            }
        }

        points = mutableNaivePoints.map { GCJ02MainlandChinaPoint(it) }.toImmutableList()
    }

    override fun toString() = TAG

    private companion object {
        private const val TAG = "GoogleMapsHtmlInput"
    }
}
