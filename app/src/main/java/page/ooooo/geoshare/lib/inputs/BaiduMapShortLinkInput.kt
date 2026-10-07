package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.network.FetchTools
import page.ooooo.geoshare.lib.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BaiduMapShortLinkInput @Inject constructor(
    private val baiduMapUriInput: dagger.Lazy<BaiduMapUriInput>,
    val engine: HttpClientEngine,
    val log: Log,
    val uriQuote: UriQuote,
) : BasicInput, Input.HasPattern, Input.HasPermission {
    override fun getName(resources: Resources) = resources.getString(R.string.input_baidu_map_short_link_name)
    override val group = InputGroup.BAIDU_MAP
    override val changelog = persistentListOf(
        InputChangelogItem.Url(35, "https://j.map.baidu.com"),
    )

    override val pattern = Regex("""((?:https?://)?j\.map\.baidu\.com/\S+)""")

    override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
        fetchTools.headLocationHeader(match, engine, log, uriQuote).run {
            parseResult {
                next = MatchedInput(baiduMapUriInput.get(), this@run.toString())
            }
        }

    override fun toString() = "BaiduMapShortLinkInput"
}
