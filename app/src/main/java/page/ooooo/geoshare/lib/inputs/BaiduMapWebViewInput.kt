package page.ooooo.geoshare.lib.inputs

import android.webkit.WebSettings
import androidx.annotation.StringRes
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.BD09MCPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.network.DefaultNetworkTools

object BaiduMapWebViewInput : WebViewInput {

    @Serializable
    private data class ExtractedPoint(val lat: Double?, val lon: Double?, val z: Double?, val name: String?)

    @StringRes
    override val permissionTitleResId = R.string.converter_baidu_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_baidu_map_loading_indicator_title

    // language=JavaScript
    override val unsafeExtractionJavascript = """
        () => {
            function deepGet(obj, ...keys) {
                return keys.reduce((acc, key) => {
                    if (acc === null || acc === undefined) return undefined;
                    return acc[key];
                }, obj);
            }
            const lat =
                deepGet(window, '_indoorMgr', '_map', 'temp', 'infoWin', 'overlay', 'point', 'lat') ||
                deepGet(window, '_appStateFromUrl', 'loc', 'y');
            const lon =
                deepGet(window, '_indoorMgr', '_map', 'temp', 'infoWin', 'overlay', 'point', 'lng') ||
                deepGet(window, '_appStateFromUrl', 'loc', 'x');
            const z = deepGet(window, '_appStateFromUrl', 'loc', 'z');
            const name = deepGet(window, '_appStateFromUrl', 'wd', 0);
            return JSON.stringify({ lat, lon, z, name });
        };
    """.trimIndent()

    override suspend fun parse(
        data: String,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        val json = Json {
            explicitNulls = false
        }
        try {
            json.decodeFromString<ExtractedPoint>(data)
        } catch (tr: IllegalArgumentException) {
            log.e(TAG, "Deserialization error", tr)
            null
        }?.run {
            points = persistentListOf(BD09MCPoint(lat, lon, z, name, source = Source.JAVASCRIPT))
        }
    }

    override fun extendWebSettings(settings: WebSettings) {
        settings.domStorageEnabled = true
        settings.userAgentString = DefaultNetworkTools.DESKTOP_USER_AGENT
    }

    override fun shouldInterceptRequest(requestUrlString: String) =
        // Assets
        requestUrlString.endsWith(".css")
            || requestUrlString.endsWith(".ico")
            || (requestUrlString.endsWith(".png") && !requestUrlString.contains("/image/api/"))
            || requestUrlString.endsWith("/static/common/images/new/loading")

            // Map tiles
            || requestUrlString.contains(@Suppress("SpellCheckingInspection") "bdimg.com/tile/")

            // Tracking
            || requestUrlString.contains(@Suppress("SpellCheckingInspection") "/alog.min.js")
            || requestUrlString.contains(@Suppress("SpellCheckingInspection") "map.baidu.com/newmap_test/static/common/images/transparent.gif")

    private const val TAG = "BaiduMapWebViewInput"

    override fun toString() = TAG
}
