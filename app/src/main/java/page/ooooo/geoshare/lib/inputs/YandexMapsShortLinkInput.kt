package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.network.FetchTools
import page.ooooo.geoshare.lib.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YandexMapsShortLinkInput @Inject constructor(
    private val yandexMapsUriInput: dagger.Lazy<YandexMapsUriInput>,
    val engine: HttpClientEngine,
    val log: Log,
    val uriQuote: UriQuote,
) : BasicInput, Input.HasPattern, Input.HasPermission {
    override fun getName(resources: Resources) = resources.getString(R.string.input_yandex_short_link_name)
    override val group = InputGroup.YANDEX_MAPS

    override val pattern = Regex("""((?:https?://)?yandex(?:\.[a-z]{2,3})?\.[a-z]{2,3}/maps/-/\S+)""")

    override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
        fetchTools.headLocationHeader(match, engine, log, uriQuote).run {
            parseResult {
                next = MatchedInput(yandexMapsUriInput.get(), this@run.toString())
            }
        }

    override fun toString() = "YandexMapsShortLinkInput"
}
