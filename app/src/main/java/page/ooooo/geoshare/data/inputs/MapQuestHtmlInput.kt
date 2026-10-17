package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.extensions.toLatLonPoint
import page.ooooo.geoshare.data.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.log.Log
import page.ooooo.geoshare.data.network.DESKTOP_USER_AGENT
import page.ooooo.geoshare.data.network.FetchTools
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapQuestHtmlInput @Inject constructor(
    private val engine: HttpClientEngine,
    private val log: Log,
    private val uriQuote: UriQuote,
) : BasicOnlineInput {
    override fun getName(resources: Resources) = resources.getString(R.string.input_map_quest_html_name)
    override val group = InputGroup.MAP_QUEST

    override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
        fetchTools.getBodyAsChannel(
            match,
            engine,
            log,
            uriQuote,
            // Set a custom user agent, so that MapQuest doesn't return empty 202 Accepted response
            userAgent = DESKTOP_USER_AGENT,
        ) { data ->
            parseResultAsync {
                val latLonPattern = Regex(""""latitude":$LAT,"longitude":$LON""")

                while (true) {
                    val line = data.readLine() ?: break
                    latLonPattern.find(line)?.toLatLonPoint(source = Source.HTML_NOT_SEMANTIC)?.let {
                        points = persistentListOf(GCJ02MainlandChinaPoint(it))
                        return@parseResultAsync
                    }
                }
            }
        }

    override fun toString() = "MapQuestHtmlInput"
}
