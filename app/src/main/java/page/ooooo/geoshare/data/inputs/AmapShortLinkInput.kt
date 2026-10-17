package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.log.Log
import page.ooooo.geoshare.data.network.FetchTools
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmapShortLinkInput @Inject constructor(
    private val amapUriInput: dagger.Lazy<AmapUriInput>,
    private val engine: HttpClientEngine,
    private val log: Log,
    private val uriQuote: UriQuote,
) : BasicOnlineInput, Input.HasPattern {
    override fun getName(resources: Resources) = resources.getString(R.string.input_amap_short_link_name)
    override val group = AmapInputGroup
    override val changelog = persistentListOf(
        InputChangelogItem.Url(27, "https://surl.amap.com/"),
    )

    override val pattern = Regex("""((?:https?://)?surl\.amap\.com/\S+)""")

    override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
        fetchTools.headLocationHeader(match, engine, log, uriQuote).run {
            parseResult {
                next = MatchedInput(amapUriInput.get(), this@run.toString())
            }
        }

    override fun toString() = "AmapShortLinkInput"
}
