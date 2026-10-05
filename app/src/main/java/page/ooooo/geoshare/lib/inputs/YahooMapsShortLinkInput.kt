package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YahooMapsShortLinkInput @Inject constructor(
    private val yahooMapsUriInput: dagger.Lazy<YahooMapsUriInput>,
    override val engine: HttpClientEngine,
    override val log: Log,
    override val uriQuote: UriQuote,
) : HeadLocationHeaderInput {
    override fun getName(resources: Resources) = resources.getString(R.string.input_yahoo_maps_short_link_name)
    override val group = InputGroup.YAHOO_MAPS
    override val changelog = persistentListOf(
        InputChangelogItem.Url(51, "https://yahoo.jp"),
    )

    override val pattern = Regex("""((?:https?://)?yahoo\.jp/\S+)""")

    override suspend fun parse(data: Uri, match: String, resources: Resources) = parseResult {
        next = MatchedInput(yahooMapsUriInput.get(), data.toString())
    }

    override fun toString() = "YahooMapsShortLinkInput"
}
