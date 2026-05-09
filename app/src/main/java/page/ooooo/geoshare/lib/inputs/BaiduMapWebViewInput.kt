package page.ooooo.geoshare.lib.inputs

import android.webkit.WebSettings
import androidx.annotation.StringRes
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.network.NetworkTools

object BaiduMapWebViewInput : WebViewInput {
    @StringRes
    override val permissionTitleResId = R.string.converter_baidu_map_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_baidu_map_loading_indicator_title

    override suspend fun parse(data: String, prevPoints: Points?, uriQuote: UriQuote, log: ILog) = buildParseResult {
        nextInput = BaiduMapUriInput
        nextMatch = data
    }

    override fun extendWebSettings(settings: WebSettings) {
        settings.domStorageEnabled = true
        settings.userAgentString = NetworkTools.DESKTOP_USER_AGENT
    }

    override fun shouldInterceptRequest(requestUrlString: String) =
        // Assets
        requestUrlString.endsWith(".ico")
            || (requestUrlString.endsWith(".png") && !requestUrlString.contains("/image/api/"))
            || requestUrlString.endsWith("/static/common/images/new/loading")
            // Notice that we don't block .css, so that links such as https://j.map.baidu.com/a7/GXfM redirect to the
            // correct URL

            // Map tiles
            || requestUrlString.contains(@Suppress("SpellCheckingInspection") "bdimg.com/tile/")

            // Tracking
            || requestUrlString.contains(@Suppress("SpellCheckingInspection") "/alog.min.js")
            || requestUrlString.contains(@Suppress("SpellCheckingInspection") "map.baidu.com/newmap_test/static/common/images/transparent.gif")
}
