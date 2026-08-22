package page.ooooo.geoshare.lib.inputs

import android.util.Log
import android.webkit.WebSettings
import androidx.annotation.StringRes
import org.json.JSONObject
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
     * Extracts the URL of the page, but only if the JavaScript of the page has changed the URL.
     *
     * TODO Document why startsWith()
     */
    // language=JavaScript
    override fun getUnsafeExtractionJavaScript(match: String) = """
        () => !location.href.startsWith(${JSONObject.quote(match)}) ? location.href : undefined;
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
