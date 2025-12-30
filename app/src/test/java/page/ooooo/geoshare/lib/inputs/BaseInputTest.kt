package page.ooooo.geoshare.lib.inputs

import io.ktor.utils.io.jvm.javaio.*
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.position.Position

abstract class BaseInputTest() {
    protected abstract val input: Input

    protected var log: ILog = FakeLog
    protected var uriQuote: UriQuote = FakeUriQuote()

    fun getUri(uriString: String): String? = (input.uriPattern find uriString)?.group()

    fun doesUriPatternMatch(uriString: String): Boolean = input.uriPattern.matches(uriString)

    fun getShortUri(uriString: String): String? =
        ((input as Input.HasShortUri).shortUriPattern match uriString)?.group()

    fun isShortUri(uriString: String): Boolean = getShortUri(uriString) != null

    suspend fun parseUri(uriString: String) = input.parseUri(Uri.parse(uriString, uriQuote))

    suspend fun parseHtml(html: String) = (input as Input.HasHtml).parseHtml(
        channel = html.byteInputStream().toByteReadChannel(),
        positionFromUri = Position(),
        log = log,
    )
}
