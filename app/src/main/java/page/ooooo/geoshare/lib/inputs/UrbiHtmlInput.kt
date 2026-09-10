package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.decodeBasicHtmlEntities
import page.ooooo.geoshare.lib.extensions.groupOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UrbiHtmlInput @Inject constructor(
    private val urbiUriInput: dagger.Lazy<UrbiUriInput>,
    override val engine: HttpClientEngine,
    override val log: Log,
    override val uriQuote: UriQuote,
) : BodyAsChannelInput {
    override fun getName(resources: Resources) = resources.getString(R.string.input_urbi_html_name)
    override val group = InputGroup.URBI

    override suspend fun parse(
        data: ByteReadChannel,
        match: String,
        resources: Resources,
    ) = parseResult {
        val pattern = Regex("""property="twitter:image" content="([^"]+)""")

        while (true) {
            val line = data.readLine() ?: break
            pattern.find(line)?.groupOrNull()?.let { attr ->
                next = MatchedInput(urbiUriInput.get(), attr.decodeBasicHtmlEntities())
                return@parseResult
            }
        }
    }

    override fun toString() = "UrbiHtmlInput"
}
