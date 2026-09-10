package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Uri

interface InputTest {
    val resources: Resources

    suspend fun TextInput.parse(text: String, match: String = "https://example.com/") =
        this.parse(text, match, resources)

    suspend fun UriInput.parse(uriString: String, match: String = uriString) =
        this.parse(Uri.parse(uriString, uriQuote = FakeUriQuote), match, resources)

    suspend fun WebViewInput.parse(data: String, match: String = "https://example.com/") =
        this.parse(data, match, resources)

    suspend fun <T> BasicInput<T>.fetchAndParse(match: String): ParseResult =
        fetch(match) { data -> parse(data, match, resources) }

    suspend fun BodyAsChannelInput.parse(html: String, match: String = "https://example.com/") =
        this.parse(html.byteInputStream().toByteReadChannel(), match, resources)

    suspend fun BodyAsTextInput.parse(body: String, match: String = "https://example.com/") =
        this.parse(body, match, resources)
}
