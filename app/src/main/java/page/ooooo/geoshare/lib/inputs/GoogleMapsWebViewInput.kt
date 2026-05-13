package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote

object GoogleMapsWebViewInput : WebViewInput {

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title

    // Quickly check that the URL isn't an intermediate URL with zero coordinates, e.g.
    // https://www.google.com/maps/dir//{name}/@0,0,{zoom}z/data=...
    // language=JavaScript
    override val unsafeExtractionJavascript = """
        () =>
            window.location.href !== 'about:blank' && !window.location.href.includes('/@0,0,')
                ? window.location.href
                : undefined;
    """.trimIndent()

    override suspend fun parse(
        data: String,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        nextStep = NextStep(GoogleMapsUriInput, data)
    }

    override fun shouldInterceptRequest(requestUrlString: String) =
        // Assets
        requestUrlString.endsWith(".gif")
            || requestUrlString.endsWith(".ico")
            || requestUrlString.endsWith(".png")
            || requestUrlString.endsWith(".svg")
            || requestUrlString.contains(@Suppress("SpellCheckingInspection") "fonts.gstatic.com/")
            || requestUrlString.contains(@Suppress("SpellCheckingInspection") "maps.gstatic.com/")
            || requestUrlString.contains(@Suppress("SpellCheckingInspection") "googleusercontent.com/")
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
            || requestUrlString.contains(@Suppress("SpellCheckingInspection") "googlesyndication.com/")

            // Something that is requested too many times
            || requestUrlString.contains("/maps/res/CompactLegend-Roadmap-")

    override fun toString() = "GoogleMapsWebViewInput"
}
