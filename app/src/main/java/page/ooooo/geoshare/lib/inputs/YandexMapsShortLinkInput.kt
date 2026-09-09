package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YandexMapsShortLinkInput @Inject constructor(
    private val yandexMapsUriInput: dagger.Lazy<YandexMapsUriInput>,
    override val engine: HttpClientEngine,
    override val log: Log,
    override val uriQuote: UriQuote,
) : HeadLocationHeaderInput {
    override fun getName(resources: Resources) = "Yandex Short Link"

    override val pattern = Regex("""((?:https?://)?yandex(?:\.[a-z]{2,3})?\.[a-z]{2,3}/maps/-/\S+)""")

    override suspend fun parse(data: Uri, match: String, resources: Resources) = parseResult {
        next = MatchedInput(yandexMapsUriInput.get(), data.toString())
    }

    override fun toString() = "YandexMapsShortLinkInput"
}
