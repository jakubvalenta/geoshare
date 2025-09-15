package page.ooooo.geoshare

import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.converters.UrlConverter
import page.ooooo.geoshare.lib.groupOrNull

abstract class BaseUrlConverterTest() {
    protected abstract val urlConverter: UrlConverter

    private var uriQuote: UriQuote = FakeUriQuote()

    fun getUri(uriString: String): String? =
        urlConverter.uriPattern.matcher(uriString)?.takeIf { it.find() }?.group()

    fun isSupportedUrl(uriString: String): Boolean = urlConverter.uriPattern.matches(uriString)

    fun getShortUri(uriString: String): String? =
        if (urlConverter is UrlConverter.WithShortUriPattern) {
            (urlConverter as UrlConverter.WithShortUriPattern).let { urlConverter ->
                urlConverter.shortUriPattern.matcher(uriString)?.takeIf { it.matches() }?.let {
                    if (urlConverter.shortUriReplacement != null) {
                        it.replaceFirst(urlConverter.shortUriReplacement)
                    } else {
                        it.group()
                    }
                }
            }
        } else {
            throw NotImplementedError()
        }

    fun isShortUrl(uriString: String): Boolean = getShortUri(uriString) != null

    fun parseUrl(uriString: String): Position? = if (urlConverter is UrlConverter.WithUriPattern) {
        (urlConverter as UrlConverter.WithUriPattern).conversionUriPattern.matches(Uri.parse(uriString, uriQuote))
            ?.let { conversionMatchers ->
                Position(
                    conversionMatchers.groupOrNull("lat"),
                    conversionMatchers.groupOrNull("lon"),
                    conversionMatchers.groupOrNull("q"),
                    conversionMatchers.groupOrNull("z")
                )
            }
    } else {
        throw NotImplementedError()
    }

    fun parseHtml(html: String): Position? = if (urlConverter is UrlConverter.WithHtmlPattern) {
        (urlConverter as UrlConverter.WithHtmlPattern).conversionHtmlPattern?.matches(html)?.let { conversionMatchers ->
            Position(
                conversionMatchers.groupOrNull("lat"),
                conversionMatchers.groupOrNull("lon"),
                conversionMatchers.groupOrNull("q"),
                conversionMatchers.groupOrNull("z")
            )
        }
    } else {
        throw NotImplementedError()
    }

    fun parseHtmlRedirect(html: String) = if (urlConverter is UrlConverter.WithHtmlPattern) {
        (urlConverter as UrlConverter.WithHtmlPattern).conversionHtmlRedirectPattern?.matches(html)?.groupOrNull("url")
    } else {
        throw NotImplementedError()
    }
}
