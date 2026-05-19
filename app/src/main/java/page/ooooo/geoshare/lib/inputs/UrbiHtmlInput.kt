package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
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
) : BodyAsChannelInput {
    @StringRes
    override val permissionTitleResId = R.string.converter_urbi_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_urbi_loading_indicator_title

    override suspend fun parse(
        data: ByteReadChannel,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        val pattern = Regex("""property="twitter:image" content="([^"]+)""")

        // Notice that unlike in other Inputs, we don't copy any point names from prevResult here

        while (true) {
            val line = data.readLine() ?: break
            pattern.find(line)?.groupOrNull()?.let { attr ->
                nextStep = NextStep(urbiUriInput.get(), attr.decodeBasicHtmlEntities())
                return@buildParseResult
            }
        }
    }

    override fun toString() = "UrbiHtmlInput"
}
