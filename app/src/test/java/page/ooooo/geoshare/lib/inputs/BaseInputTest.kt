package page.ooooo.geoshare.lib.inputs

import android.R.id.input
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.position.Position

abstract class BaseInputTest() {
    protected abstract val input: Input

    protected var uriQuote: UriQuote = FakeUriQuote()

    fun getUri(uriString: String): String? = input.uriPattern.matcher(uriString).takeIf { it.find() }?.group()

    fun doesUriPatternMatch(uriString: String): Boolean = input.uriPattern.matches(uriString)

    fun getShortUri(uriString: String): String? = (input as Input.HasShortUri)
        .shortUriPattern.matcher(uriString)?.takeIf { it.matches() }?.group()

    fun isShortUri(uriString: String): Boolean = getShortUri(uriString) != null

    fun parseUrl(uriString: String): Position? = (input as Input.HasUri)
        .conversionUriPattern.matches(Uri.parse(uriString, uriQuote))?.toPosition()

    fun parseHtml(html: String): Position? = (input as Input.HasHtml)
        .conversionHtmlPattern?.matches(html)?.toPosition()

    fun parseHtmlRedirect(html: String) = (input as Input.HasHtml)
        .conversionHtmlRedirectPattern?.matches(html)?.toUrlString()
}
