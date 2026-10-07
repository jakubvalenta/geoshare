package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import android.webkit.WebSettings
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.network.DESKTOP_USER_AGENT
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsWebViewInput @Inject constructor(
    private val googleMapsUriInput: dagger.Lazy<GoogleMapsUriInput>,
) : WebViewInput {
    override fun getName(resources: Resources) = resources.getString(R.string.input_google_maps_web_view_name)
    override val group = InputGroup.GOOGLE_MAPS

    /**
     * Extracts the URL of the page after the page JavaScript has changed the URL.
     *
     * It waits for a while after a URL change and returns the URL only if no further URL change happens. The reaason is
     * that the first URL change often doesn't lead to the final coordinates.
     *
     * It does only a quick check that the URL contains coordinates, so that it doesn't duplicate the Google Maps URI
     * parsing code, and because we know that:
     *
     * - The URL will most probably be in format `/@{lat},{lon},{z}z`
     * - The URL could possibly be in format `/data=...!3d{lat}!4d{lon}`
     * - The URL is unlikely to be in another format such as `/?ll={lat},{lon}`
     */
    // language=JavaScript
    override fun getUnsafeExtractionJavaScript() = """
        () => {
            if (window.__hrefWithCoordinates !== location.href) {
                if (location.href.includes("/@") || location.href.includes("!2d") || location.href.includes("!4d")) {
                    window.__hrefWithCoordinates = location.href;
                    clearTimeout(window.__hrefWithCoordinatesTimeout);
                    window.__hrefWithCoordinatesTimeout = setTimeout(
                        () => window.__hrefWithCoordinatesFinished = true,
                        3000
                    );
                }
            } else if (window.__hrefWithCoordinatesFinished) {
                return window.__hrefWithCoordinates;
            }
            return undefined;
        }
    """.trimIndent()

    override suspend fun parse(data: String, match: String, resources: Resources) = parseResult {
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
