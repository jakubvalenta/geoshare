package page.ooooo.geoshare.lib.inputs

import io.ktor.utils.io.jvm.javaio.*
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote

abstract class BaseInputTest {
    protected abstract val input: Input

    protected var log: ILog = FakeLog
    protected var uriQuote: UriQuote = FakeUriQuote()

    fun getUri(uriString: String): String? = input.uriPattern.find(uriString)?.value

    fun doesUriPatternMatch(uriString: String): Boolean = input.uriPattern.find(uriString) != null

    fun getShortUri(uriString: String): String? =
        (input as Input.HasShortUri).shortUriPattern.matchEntire(uriString)?.value

    fun isShortUri(uriString: String): Boolean = getShortUri(uriString) != null

    suspend fun parseUri(uriString: String) = input.parseUri(Uri.parse(uriString, uriQuote))

    suspend fun parseHtml(html: String, htmlUrlString: String = "https://example.com/") =
        (input as Input.HasHtml).parseHtml(
            htmlUrlString = htmlUrlString,
            channel = html.byteInputStream().toByteReadChannel(),
            pointsFromUri = persistentListOf(),
            log = log,
        )
}
