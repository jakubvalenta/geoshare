package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.ConversionHtmlPattern
import page.ooooo.geoshare.lib.ConversionUriPattern

sealed interface UrlConverter {
    val name: String
    val uriPattern: Pattern

    interface WithShortUriPattern : UrlConverter {
        val shortUriPattern: Pattern
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }

    interface WithUriPattern : UrlConverter {
        val conversionUriPattern: ConversionUriPattern
    }

    interface WithHtmlPattern : UrlConverter {
        val conversionHtmlPattern: ConversionHtmlPattern?
        val conversionHtmlRedirectPattern: ConversionHtmlPattern?
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }
}
