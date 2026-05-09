package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.WGS84Point

/**
 * @see DebugInput
 */
object DebugWebViewInput : WebViewInput {
    @StringRes
    override val permissionTitleResId = R.string.converter_debug_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_debug_loading_indicator_title

    override suspend fun parse(
        data: String,
        match: String,
        prevPoints: Points?,
        uriQuote: UriQuote,
        log: ILog,
    ) = buildParseResult {
        points = persistentListOf(WGS84Point(NaivePoint.genRandomPoint()))
    }
}
