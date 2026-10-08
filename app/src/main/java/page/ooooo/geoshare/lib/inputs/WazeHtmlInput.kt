package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.network.FetchTools
import page.ooooo.geoshare.lib.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WazeHtmlInput @Inject constructor(
    val engine: HttpClientEngine,
    val log: Log,
    val uriQuote: UriQuote,
) : BasicOnlineInput {
    override fun getName(resources: Resources) = resources.getString(R.string.input_waze_html_name)
    override val group = InputGroup.WAZE

    override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
        fetchTools.getBodyAsChannel(match, engine, log, uriQuote) { data ->
            parseResultAsync {
                val pattern = Regex(""""latLng":\{"lat":$LAT,"lng":$LON\}""")

                while (true) {
                    val line = data.readLine() ?: break
                    pattern.find(line)?.toLatLonPoint(Source.JAVASCRIPT)?.let {
                        points = persistentListOf(WGS84Point(it))
                        return@parseResultAsync
                    }
                }
            }
        }

    override fun toString() = "WazeHtmlInput"
}
