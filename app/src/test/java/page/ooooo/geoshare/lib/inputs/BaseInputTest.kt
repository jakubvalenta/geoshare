package page.ooooo.geoshare.lib.inputs

import kotlinx.io.asSource
import kotlinx.io.buffered
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.position.Position

abstract class BaseInputTest() {
    protected abstract val input: Input

    protected var uriQuote: UriQuote = FakeUriQuote()

    fun getUri(uriString: String): String? = (input.uriPattern find uriString)?.group()

    fun doesUriPatternMatch(uriString: String): Boolean = input.uriPattern.matches(uriString)

    fun getShortUri(uriString: String): String? =
        ((input as Input.HasShortUri).shortUriPattern match uriString)?.group()

    fun isShortUri(uriString: String): Boolean = getShortUri(uriString) != null

    fun parseUri(uriString: String): Pair<Position, String?> =
        input.parseUri(Uri.parse(uriString, uriQuote)).toPair()

    fun parseUriGetPosition(uriString: String): Position? =
        input.parseUri(Uri.parse(uriString, uriQuote)).position
            .takeIf { !it.points.isNullOrEmpty() || it.q != null || it.z != null }

    fun parseHtml(html: String): Pair<Position, String?> =
        (input as Input.HasHtml).parseHtml(html.byteInputStream().asSource().buffered()).toPair()

    fun parseHtmlGetPosition(html: String): Position? =
        (input as Input.HasHtml).parseHtml(html.byteInputStream().asSource().buffered()).position
            .takeIf { it.mainPoint != null || it.q != null || it.z != null }
}
