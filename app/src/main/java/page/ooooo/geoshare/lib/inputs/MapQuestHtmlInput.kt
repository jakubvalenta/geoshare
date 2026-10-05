package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.uri.UriQuote
import page.ooooo.geoshare.lib.extensions.toLatLonPoint
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.network.DESKTOP_USER_AGENT
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapQuestHtmlInput @Inject constructor(
    override val engine: HttpClientEngine,
    override val log: Log,
    override val uriQuote: UriQuote,
) : BodyAsChannelInput {
    override fun getName(resources: Resources) = resources.getString(R.string.input_map_quest_html_name)
    override val group = InputGroup.MAP_QUEST

    // Set a custom user agent, so that MapQuest doesn't return empty 202 Accepted response
    override val userAgent = DESKTOP_USER_AGENT

    override suspend fun parse(data: ByteReadChannel, match: String, resources: Resources) = parseResult {
        val latLonPattern = Regex(""""latitude":$LAT,"longitude":$LON""")

        while (true) {
            val line = data.readLine() ?: break
            latLonPattern.find(line)?.toLatLonPoint(source = Source.HTML)?.let {
                points = persistentListOf(GCJ02MainlandChinaPoint(it))
                return@parseResult
            }
        }
    }

    override fun toString() = "MapQuestHtmlInput"
}
