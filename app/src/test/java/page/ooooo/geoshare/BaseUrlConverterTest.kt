package page.ooooo.geoshare

import android.R.attr.host
import android.R.attr.path
import android.R.attr.scheme
import android.net.Uri
import com.google.re2j.Pattern
import org.junit.Before
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.converters.UrlConverter
import page.ooooo.geoshare.lib.groupOrNull

fun mockUri(uriString: String): Uri =
    Pattern.compile("""(?P<scheme>[^:]*):?(//)?(?P<host>[^/?]*)(?P<path>[^?]*)(\?(?P<query>.*))?""")
        .matcher(uriString)?.takeIf { it.matches() }?.let { m ->
            mock {
                on { scheme } doReturn m.group("scheme")
                on { host } doReturn m.group("host")
                on { path } doReturn m.group("path")
                on { query } doReturn m.group("query")
                on { toString() } doReturn uriString
            }
        } ?: throw Exception("Invalid URI")

open class BaseUrlConverterTest() {
    protected lateinit var urlConverter: UrlConverter
    private lateinit var uriQuote: UriQuote

    @Before
    fun before() {
        uriQuote = FakeUriQuote()
    }

    fun isSupportedUrl(uriString: String): Boolean = urlConverter.uriPattern.matches(uriString)

    fun isShortUrl(uriString: String): Boolean = urlConverter.shortUriPattern?.matches(uriString) == true

    fun parseUrl(uriString: String): Position? =
        urlConverter.conversionUriPattern.matches(mockUri(uriString), uriQuote)?.let { conversionMatchers ->
            Position(
                conversionMatchers.groupOrNull("lat"),
                conversionMatchers.groupOrNull("lon"),
                conversionMatchers.groupOrNull("q"),
                conversionMatchers.groupOrNull("z")
            )
        }

    fun parseHtml(html: String): Position? =
        urlConverter.conversionHtmlPattern?.matches(html)?.let { conversionMatchers ->
            Position(
                conversionMatchers.groupOrNull("lat"),
                conversionMatchers.groupOrNull("lon"),
                conversionMatchers.groupOrNull("q"),
                conversionMatchers.groupOrNull("z")
            )
        }

    fun parseHtmlRedirect(html: String): String? =
        urlConverter.conversionHtmlRedirectPattern?.matches(html)?.groupOrNull("url")
}
