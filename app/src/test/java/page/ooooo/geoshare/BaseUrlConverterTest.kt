package page.ooooo.geoshare

import android.net.Uri
import com.google.re2j.Pattern
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.converters.UrlConverter
import page.ooooo.geoshare.lib.groupOrNull

fun mockUri(uriString: String): Uri =
    Pattern.compile("""(?P<scheme>[^:]*):?(//)?(?P<host>[^/?]*)(?P<path>[^?]*)(\?(?P<query>.*))?""").matcher(uriString)
        ?.takeIf { it.matches() }?.let { m ->
            mock {
                on { scheme } doReturn m.group("scheme")
                on { host } doReturn m.group("host")
                on { path } doReturn m.group("path")
                on { query } doReturn m.group("query")
                on { toString() } doReturn uriString
            }
        } ?: throw Exception("Invalid URI")

abstract class BaseUrlConverterTest() {
    protected abstract val urlConverter: UrlConverter

    private var uriQuote: UriQuote = FakeUriQuote()

    fun isSupportedUrl(uriString: String): Boolean = urlConverter.uriPattern.matches(uriString)

    fun isShortUrl(uriString: String): Boolean = if (urlConverter is UrlConverter.WithShortUriPattern) {
        (urlConverter as UrlConverter.WithShortUriPattern).shortUriPattern.matches(uriString)
    } else {
        throw NotImplementedError()
    }

    fun parseUrl(uriString: String): Position? = if (urlConverter is UrlConverter.WithUriPattern) {
        (urlConverter as UrlConverter.WithUriPattern).conversionUriPattern.matches(mockUri(uriString), uriQuote)
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
