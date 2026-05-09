package page.ooooo.geoshare.lib.inputs

import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Uri

interface InputTest {
    suspend fun UriInput.parse(uriString: String) =
        this.parse(Uri.parse(uriString, uriQuote = FakeUriQuote), uriQuote = FakeUriQuote)

    suspend fun BodyAsChannelInput.parse(html: String, match: String = "https://example.com/") =
        // TODO Use match
        this.parse(html.byteInputStream().toByteReadChannel(), uriQuote = FakeUriQuote, log = FakeLog)
}
