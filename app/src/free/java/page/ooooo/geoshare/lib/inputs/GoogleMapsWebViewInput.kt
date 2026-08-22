package page.ooooo.geoshare.lib.inputs

import android.webkit.WebSettings
import androidx.annotation.StringRes
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.network.DESKTOP_USER_AGENT
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsWebViewInput @Inject constructor(
    private val googleMapsUriInput: dagger.Lazy<GoogleMapsUriInput>,
) : WebViewInput {
    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title

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

    override suspend fun parse(data: String, match: String) = parseResult {
        next = MatchedInput(googleMapsUriInput.get(), data)
    }

    override fun extendWebSettings(settings: WebSettings) = Companion.extendWebSettings(settings)

    override fun shouldInterceptRequest(requestUrlString: String) = Companion.shouldInterceptRequest(requestUrlString)

    override fun toString() = "GoogleMapsWebViewInput"

    companion object {
        /**
         * Set custom user agent to prevent:
         *
         * - Directions getting stuck at intermediate URI with zero coordinates.
         * - Place lists showing "No list found".
         */
        fun extendWebSettings(settings: WebSettings) {
            settings.userAgentString = DESKTOP_USER_AGENT
        }

        fun shouldInterceptRequest(requestUrlString: String) =
            // Assets
            requestUrlString.endsWith(".gif")
                || requestUrlString.endsWith(".ico")
                || requestUrlString.endsWith(".png")
                || requestUrlString.endsWith(".svg")
                || requestUrlString.contains("fonts.gstatic.com/")
                || requestUrlString.contains("maps.gstatic.com/")
                || requestUrlString.contains("googleusercontent.com/")
                || requestUrlString.contains("/gps-cs-s/")
                || requestUrlString.contains("/ss/")
                || requestUrlString.contains("/thumbnail")

                // Map tiles
                || requestUrlString.contains("/kh/")
                || requestUrlString.contains("/maps/vt")

                // Tracking
                || requestUrlString.contains("/generate_204")
                || requestUrlString.contains("/log204")
                || requestUrlString.contains("google.com/gen_204")
                || requestUrlString.contains("google.com/log")
                || requestUrlString.contains("googlesyndication.com/")

                // Something that is requested too many times
                || requestUrlString.contains("/maps/res/CompactLegend-Roadmap-")
    }
}
