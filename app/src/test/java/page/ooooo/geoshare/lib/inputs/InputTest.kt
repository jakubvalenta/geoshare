package page.ooooo.geoshare.lib.inputs

import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Uri

interface InputTest {
    suspend fun TextInput.parse(text: String, match: String = "https://example.com/") =
        this.parse(text, match, uriQuote = FakeUriQuote, log = FakeLog)

    suspend fun UriInput.parse(uriString: String, match: String = "https://example.com/") =
        this.parse(Uri.parse(uriString, uriQuote = FakeUriQuote), match, uriQuote = FakeUriQuote)

    suspend fun BodyAsChannelInput.parse(html: String, match: String = "https://example.com/") =
        this.parse(html.byteInputStream().toByteReadChannel(), match, uriQuote = FakeUriQuote, log = FakeLog)

    suspend fun BodyAsTextInput.parse(body: String, match: String = "https://example.com/") =
        this.parse(body, match, uriQuote = FakeUriQuote, log = FakeLog)
}
