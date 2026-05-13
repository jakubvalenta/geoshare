package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

object WazeHtmlInput : BodyAsChannelInput {
    @StringRes
    override val permissionTitleResId = R.string.converter_waze_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_waze_loading_indicator_title

    override suspend fun parse(
        data: ByteReadChannel,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        val pattern = Regex(""""latLng":\{"lat":$LAT,"lng":$LON\}""")

        val name = prevResult?.points?.lastOrNull()?.name

        while (true) {
            val line = data.readLine() ?: break
            pattern.find(line)?.toLatLonPoint(Source.JAVASCRIPT)?.let {
                points = persistentListOf(WGS84Point(it).copy(name = name))
                return@buildParseResult
            }
        }
    }

    override fun toString() = "WazeHtmlInput"
}
