package page.ooooo.geoshare

import org.junit.Before
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.converters.UrlConverter
import page.ooooo.geoshare.lib.getUrlQueryParams
import java.net.URL

open class BaseUrlConverterTest() {
    protected lateinit var urlConverter: UrlConverter
    private lateinit var uriQuote: UriQuote

    @Before
    fun before() {
        uriQuote = FakeUriQuote()
    }

    fun isSupportedUrl(url: URL): Boolean = urlConverter.host.matches(url.host)

    fun isShortUrl(url: URL): Boolean = urlConverter.shortUrlHost?.matches(url.host) == true

    fun parseUrl(url: URL): Position? = urlConverter.urlPattern.matches(
        url.host,
        uriQuote.decode(url.path),
        getUrlQueryParams(url.query, uriQuote),
    )?.toPosition()

    fun parseHtml(html: String): Position? = urlConverter.htmlPattern?.matches(html)?.toPosition()

    fun parseHtmlRedirect(html: String): String? =
        urlConverter.htmlRedirectPattern?.matches(html)?.getGroupOrNull("url")
}
