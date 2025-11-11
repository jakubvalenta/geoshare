package page.ooooo.geoshare.lib.inputs

import kotlinx.io.asSource
import kotlinx.io.buffered
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.merge

abstract class BaseInputTest() {
    protected abstract val input: Input

    protected var uriQuote: UriQuote = FakeUriQuote()

    fun getUri(uriString: String): String? = (input.uriPattern find uriString)?.group()

    fun doesUriPatternMatch(uriString: String): Boolean = input.uriPattern.matches(uriString)

    fun getShortUri(uriString: String): String? =
        ((input as Input.HasShortUri).shortUriPattern match uriString)?.group()

    fun isShortUri(uriString: String): Boolean = getShortUri(uriString) != null

    fun parseUrl(uriString: String): Position? =
        (input as Input.HasUri).conversionUriPattern.match(Uri.parse(uriString, uriQuote))?.merge()

    fun parseHtml(html: String): Position? =
        html.byteInputStream().asSource().buffered().use { source ->
            (input as Input.HasHtml).conversionHtmlPattern?.match(source)?.merge()
        }

    fun parseHtmlRedirect(html: String) =
        html.byteInputStream().asSource().buffered().use { source ->
            (input as Input.HasHtml).conversionHtmlRedirectPattern?.match(source)?.lastOrNull()
        }
}
