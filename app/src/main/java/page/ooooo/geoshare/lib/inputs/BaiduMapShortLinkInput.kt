package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BaiduMapShortLinkInput @Inject constructor(
    private val baiduMapUriInput: dagger.Lazy<BaiduMapUriInput>,
    override val engine: HttpClientEngine,
    override val log: Log,
    override val uriQuote: UriQuote,
) : HeadLocationHeaderInput {
    override fun getName(resources: Resources) = resources.getString(R.string.input_baidu_map_short_link_name)
    override val group = InputGroup.BAIDU_MAP
    override val changelog = persistentListOf(
        InputChangelogItem.Url(35, "https://j.map.baidu.com"),
    )

    override val pattern = Regex("""((?:https?://)?j\.map\.baidu\.com/\S+)""")

    override suspend fun parse(data: Uri, match: String, resources: Resources) = parseResult {
        next = MatchedInput(baiduMapUriInput.get(), data.toString())
    }

    override fun toString() = "BaiduMapShortLinkInput"
}
