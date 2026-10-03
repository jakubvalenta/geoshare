package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import page.ooooo.geoshare.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YahooMapsWebViewInput @Inject constructor(
    private val yahooMapsUriInput: dagger.Lazy<YahooMapsUriInput>,
) : WebViewInput {
    override fun getName(resources: Resources) = resources.getString(R.string.input_yahoo_maps_web_view_name)
    override val group = InputGroup.YAHOO_MAPS

    /**
     * Extracts the URL of the page.
     *
     * Returns undefined if the URL doesn't contain coordinates, so that the extraction is retried until the page
     * JavaScript changes the URL into one with coordinates.
     *
     * The check whether the URL contains coordinates is very simple, because we don't want to reimplement the whole URI
     * parsing here, and because we know that:
     *
     * - The URL will most probably be in format `/@{lat},{lon},{z}z`
     * - The URL could plausibly be in format `/data=...!3d{lat}!4d{lon}`
     * - The URL is unlikely to be in another format such as `/?ll={lat},{lon}`
     */
    // language=JavaScript
    override fun getUnsafeExtractionJavaScript(match: String) = """
        () => location.href.includes("/@") || location.href.includes("!2d") || location.href.includes("!4d")
            ? location.href
            : undefined;
    """.trimIndent()

    override suspend fun parse(data: String, match: String, resources: Resources) = parseResult {
        next = MatchedInput(yahooMapsUriInput.get(), data)
    }

    override fun shouldInterceptRequest(requestUrlString: String) =
        // Assets
        requestUrlString.endsWith(".css")
            || requestUrlString.endsWith(".ico")
            || requestUrlString.contains(".webp")
            || requestUrlString.contains("api.mapbox.com/fonts")
            || requestUrlString.contains("api.mapbox.com/styles")

            // Map tiles
            || requestUrlString.contains(".pbf")

            // Tracking
            || requestUrlString.contains("events.mapbox.com/")

    override fun toString() = "YahooMapsWebViewInput"
}
