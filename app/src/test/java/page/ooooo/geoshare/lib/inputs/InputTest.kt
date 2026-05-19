package page.ooooo.geoshare.lib.inputs

import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Uri

interface InputTest {
    suspend fun TextInput.parse(text: String, match: String = "https://example.com/") =
        this.parse(text, match)

    suspend fun UriInput.parse(uriString: String, match: String = uriString) =
        this.parse(Uri.parse(uriString, uriQuote = FakeUriQuote), match)

    suspend fun BodyAsChannelInput.parse(html: String, match: String = "https://example.com/") =
        this.parse(html.byteInputStream().toByteReadChannel(), match)

    suspend fun BodyAsTextInput.parse(body: String, match: String = "https://example.com/") =
        this.parse(body, match)
}
