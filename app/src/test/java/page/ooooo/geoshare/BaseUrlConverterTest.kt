package page.ooooo.geoshare

import org.junit.Before
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.converters.UrlConverter
import page.ooooo.geoshare.lib.getUrlQueryParams
import page.ooooo.geoshare.lib.groupOrNull
import java.net.URL

open class BaseUrlConverterTest() {
    protected lateinit var urlConverter: UrlConverter
    private lateinit var uriQuote: UriQuote

    @Before
    fun before() {
        uriQuote = FakeUriQuote()
    }

    fun isSupportedUrl(url: URL): Boolean = urlConverter.host.matches(url.host)

    fun isShortUrl(url: URL): Boolean = urlConverter.shortUrlPattern?.matches(url.toString()) == true

    fun parseUrl(url: URL): Position? = urlConverter.urlPattern.matches(
        url.host,
        uriQuote.decode(url.path),
        getUrlQueryParams(url.query, uriQuote),
    )?.let { conversionMatchers ->
        Position(
            conversionMatchers.groupOrNull("lat"),
            conversionMatchers.groupOrNull("lon"),
            conversionMatchers.groupOrNull("q"),
            conversionMatchers.groupOrNull("z")
        )
    }

    fun parseHtml(html: String): Position? = urlConverter.htmlPattern?.matches(html)?.let { conversionMatchers ->
        Position(
            conversionMatchers.groupOrNull("lat"),
            conversionMatchers.groupOrNull("lon"),
            conversionMatchers.groupOrNull("q"),
            conversionMatchers.groupOrNull("z")
        )
    }

    fun parseHtmlRedirect(html: String): String? = urlConverter.htmlRedirectPattern?.matches(html)?.groupOrNull("url")
}
