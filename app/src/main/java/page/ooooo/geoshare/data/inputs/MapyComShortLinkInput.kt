package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.log.Log
import page.ooooo.geoshare.data.network.FetchTools
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapyComShortLinkInput @Inject constructor(
    private val mapyComUriInput: dagger.Lazy<MapyComUriInput>,
    private val engine: HttpClientEngine,
    private val log: Log,
    private val uriQuote: UriQuote,
) : BasicOnlineInput, Input.HasPattern {
    override fun getName(resources: Resources) = resources.getString(R.string.input_mapy_com_short_link_name)
    override val group = MapyComInputGroup

    override val pattern = Regex("""((?:https?://)?(?:www\.)?mapy\.[a-z]{2,3}/s/\S+)""")

    override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
        fetchTools.getLastHopUrl(match, engine, log, uriQuote).run {
            parseResult {
                next = MatchedInput(mapyComUriInput.get(), this@run.toString())
            }
        }

    override fun toString() = "MapyComShortLinkInput"
}
