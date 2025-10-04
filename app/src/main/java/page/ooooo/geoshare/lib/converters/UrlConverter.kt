package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.*
import java.net.URL

enum class ShortUriMethod { GET, HEAD }

sealed interface UrlConverter {
    val uriPattern: Pattern
    val documentation: UrlConverterDocumentation

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
        fun getHtmlUrl(uri: Uri): URL? = uri.toUrl()
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }
}
