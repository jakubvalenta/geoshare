package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import io.ktor.utils.io.readLine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.log.Log
import page.ooooo.geoshare.data.extensions.groupOrNull
import page.ooooo.geoshare.data.network.FetchTools
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UrbiHtmlInput @Inject constructor(
    private val urbiUriInput: dagger.Lazy<UrbiUriInput>,
    val engine: HttpClientEngine,
    val log: Log,
    val uriQuote: UriQuote,
) : BasicOnlineInput {
    override fun getName(resources: Resources) = resources.getString(R.string.input_urbi_html_name)
    override val group = InputGroup.URBI

    override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
        fetchTools.getBodyAsChannel(match, engine, log, uriQuote) { data ->
            parseResultAsync {
                val pattern = Regex("""property="twitter:image" content="([^"]+)""")

                while (true) {
                    val line = data.readLine() ?: break
                    pattern.find(line)?.groupOrNull()?.let { attr ->
                        next = MatchedInput(urbiUriInput.get(), attr.decodeBasicHtmlEntities())
                        return@parseResultAsync
                    }
                }
            }
        }

    override fun toString() = "UrbiHtmlInput"
}
