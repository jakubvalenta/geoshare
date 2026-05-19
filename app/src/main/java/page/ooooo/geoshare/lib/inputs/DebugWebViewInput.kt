package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @see DebugUriInput
 */
@Singleton
class DebugWebViewInput @Inject constructor() : WebViewInput {
    @StringRes
    override val permissionTitleResId = R.string.converter_debug_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_debug_loading_indicator_title

    // language=JavaScript
    override val unsafeExtractionJavascript = """
        () => window.location.href !== 'about:blank' ? window.location.href : undefined;
    """.trimIndent()

    override suspend fun parse(
        data: String,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        points = persistentListOf(WGS84Point(NaivePoint.genRandomPoint()))
    }
}
