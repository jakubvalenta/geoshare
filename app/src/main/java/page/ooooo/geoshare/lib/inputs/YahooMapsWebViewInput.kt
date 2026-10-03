package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YahooMapsWebViewInput @Inject constructor(
    private val uriQuote: UriQuote,
) : WebViewInput {
    override fun getName(resources: Resources) = resources.getString(R.string.input_yahoo_maps_web_view_name)
    override val group = InputGroup.YAHOO_MAPS

    /**
     * Extracts the URL of the page.
     *
     * Returns undefined if the URL doesn't contain coordinates, so that the extraction is retried until the page
     * JavaScript changes the URL into one with coordinates.
     */
    // language=JavaScript
    override fun getUnsafeExtractionJavaScript(match: String) = """
        () => location.href.includes("lat=") && location.href.includes("lon=")
            ? location.href
            : undefined;
    """.trimIndent()

    override suspend fun parse(data: String, match: String, resources: Resources) = parseResult {
        Uri.parse(data, uriQuote).run {
            LAT_PATTERN.matchEntire(queryParams["lat"])?.doubleGroupOrNull()?.let { lat ->
                LON_PATTERN.matchEntire(queryParams["lon"])?.doubleGroupOrNull()?.let { lon ->
                    points = persistentListOf(WGS84Point(lat, lon, source = Source.URI))
                }
            }
        }
    }

    override fun shouldInterceptRequest(requestUrlString: String) =
        // Assets
        requestUrlString.endsWith(".css")
            || requestUrlString.endsWith(".ico")
            || requestUrlString.contains(".png")
            || requestUrlString.contains(".webp")
            || requestUrlString.contains("api.mapbox.com/fonts")
            || requestUrlString.contains("api.mapbox.com/styles")

            // Map tiles
            || requestUrlString.contains(".glb")
            || requestUrlString.contains(".pbf")

            // Tracking
            || requestUrlString.contains("events.mapbox.com/")
            || requestUrlString.contains("clb.yahoo.co.jp/")
            || requestUrlString.contains("yjtag.yahoo.co.jp/")

            // Miscellaneous
            || requestUrlString.contains("/photo")
            || requestUrlString.contains("/review")

    override fun toString() = TAG

    private companion object {
        private const val TAG = "YahooMapsWebViewInput"
    }
}
