package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.client.engine.HttpClientEngine
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WazeHtmlInput @Inject constructor(
    override val engine: HttpClientEngine,
    override val log: Log,
    override val uriQuote: UriQuote,
) : BodyAsChannelInput {
    @StringRes
    override val permissionTitleResId = R.string.converter_waze_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_waze_loading_indicator_title

    override suspend fun parse(data: ByteReadChannel, match: String, prevResult: ParseResult?) = parseResult {
        val pattern = Regex(""""latLng":\{"lat":$LAT,"lng":$LON\}""")

        val name = prevResult?.points?.lastOrNull()?.name

        while (true) {
            val line = data.readLine() ?: break
            pattern.find(line)?.toLatLonPoint(Source.JAVASCRIPT)?.let {
                points = persistentListOf(WGS84Point(it).copy(name = name))
                return@parseResult
            }
        }
    }

    override fun toString() = "WazeHtmlInput"
}
