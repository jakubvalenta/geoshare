package page.ooooo.geoshare

import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.converters.UrlConverter

abstract class BaseUrlConverterTest() {
    protected abstract val urlConverter: UrlConverter

    protected var uriQuote: UriQuote = FakeUriQuote()

    fun getUri(uriString: String): String? = urlConverter.uriPattern.matcherIfFind(uriString)?.group()

    fun doesUriPatternMatch(uriString: String): Boolean = urlConverter.uriPattern.matches(uriString)

    fun getShortUri(uriString: String): String? = if (urlConverter is UrlConverter.WithShortUriPattern) {
        (urlConverter as UrlConverter.WithShortUriPattern).let { urlConverter ->
            urlConverter.shortUriPattern.matcherIfMatches(uriString)?.group()
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
        (urlConverter as UrlConverter.WithHtmlPattern).conversionHtmlPattern?.find(html)?.toPosition()
    } else {
        throw NotImplementedError()
    }

    fun parseHtmlRedirect(html: String) = if (urlConverter is UrlConverter.WithHtmlPattern) {
        (urlConverter as UrlConverter.WithHtmlPattern).conversionHtmlRedirectPattern?.find(html)?.toUrlString()
    } else {
        throw NotImplementedError()
    }
}
