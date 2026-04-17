package page.ooooo.geoshare.lib.inputs

import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Uri

interface InputTest {
    val input: Input

    fun getUri(uriString: String): String? =
        input.uriPattern.find(uriString)?.value

    fun getShortUri(uriString: String): String? =
        (input as ShortUriInput).shortUriPattern.find(uriString)?.value

    suspend fun parseUri(uriString: String): ParseUriResult =
        input.parseUri(Uri.parse(uriString, uriQuote = FakeUriQuote), uriQuote = FakeUriQuote)

    suspend fun parseHtml(html: String, htmlUrlString: String = "https://example.com/"): ParseHtmlResult =
        (input as HtmlInput).parseHtml(
            htmlUrlString = htmlUrlString,
            channel = html.byteInputStream().toByteReadChannel(),
            pointsFromUri = persistentListOf(),
            uriQuote = FakeUriQuote,
            log = FakeLog,
        )
}
