package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.extensions.toLatLonPoint
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.log.Log
import page.ooooo.geoshare.data.network.FetchTools
import page.ooooo.geoshare.data.uri.UriQuote
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
                    pattern.find(line)?.toLatLonPoint(Source.HTML_NOT_SEMANTIC)?.let {
                        points = persistentListOf(WGS84Point(it))
                        return@parseResultAsync
                    }
                }
            }
        }

    override fun toString() = "WazeHtmlInput"
}
