package page.ooooo.geoshare.lib.converters

import page.ooooo.geoshare.lib.*

abstract class BaseUrlConverterTest() {
    protected abstract val urlConverter: UrlConverter

    protected var uriQuote: UriQuote = FakeUriQuote()

    fun getUri(uriString: String): String? = urlConverter.uriPattern.matcher(uriString).takeIf { it.find() }?.group()

    fun doesUriPatternMatch(uriString: String): Boolean = urlConverter.uriPattern.matches(uriString)

    fun getShortUri(uriString: String): String? = if (urlConverter is UrlConverter.WithShortUriPattern) {
        (urlConverter as UrlConverter.WithShortUriPattern).let { urlConverter ->
            urlConverter.shortUriPattern.matcher(uriString)?.takeIf { it.matches() }?.group()
        }
    } else {
        throw NotImplementedError()
    }

    fun isShortUri(uriString: String): Boolean = getShortUri(uriString) != null

    fun parseUrl(uriString: String): Position? = if (urlConverter is UrlConverter.WithUriPattern) {
        (urlConverter as UrlConverter.WithUriPattern).conversionUriPattern.matches(Uri.parse(uriString, uriQuote))
            ?.toPosition()
    } else {
        throw NotImplementedError()
    }

    fun parseHtml(html: String): Position? = if (urlConverter is UrlConverter.WithHtmlPattern) {
        (urlConverter as UrlConverter.WithHtmlPattern).conversionHtmlPattern?.matches(html)?.toPosition()
    } else {
        throw NotImplementedError()
    }

    fun parseHtmlRedirect(html: String) = if (urlConverter is UrlConverter.WithHtmlPattern) {
        (urlConverter as UrlConverter.WithHtmlPattern).conversionHtmlRedirectPattern?.matches(html)?.toUrlString()
    } else {
        throw NotImplementedError()
    }
}
