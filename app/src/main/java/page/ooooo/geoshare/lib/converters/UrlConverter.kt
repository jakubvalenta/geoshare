package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.*
import java.net.MalformedURLException
import java.net.URL

enum class ShortUriMethod { GET, HEAD }

sealed interface UrlConverter {
    val uriPattern: Pattern

    interface WithShortUriPattern : UrlConverter {
        val shortUriPattern: Pattern
        val shortUriMethod: ShortUriMethod get() = ShortUriMethod.HEAD
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }

    interface WithUriPattern : UrlConverter {
        val conversionUriPattern: ConversionUriPattern<PositionRegex>
    }

    interface WithHtmlPattern : UrlConverter {
        val conversionHtmlPattern: ConversionHtmlPattern<PositionRegex>? get() = null
        val conversionHtmlRedirectPattern: ConversionHtmlPattern<RedirectRegex>? get() = null

        @Throws(MalformedURLException::class)
        fun getHtmlUrl(uri: Uri): URL {
            return uri.toUrl()
        }

        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }
}
