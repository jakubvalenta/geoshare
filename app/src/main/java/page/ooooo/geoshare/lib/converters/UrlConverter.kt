package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.ConversionHtmlPattern
import page.ooooo.geoshare.lib.PositionRegex
import page.ooooo.geoshare.lib.ConversionUriPattern
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.RedirectRegex
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote

sealed interface UrlConverter {
    val uriPattern: Pattern

    interface WithShortUriPattern : UrlConverter {
        val shortUriPattern: Pattern
        val shortUriReplacement: String?
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }

    interface WithUriPattern : UrlConverter {
        val conversionUriPattern: ConversionUriPattern<PositionRegex>
    }

    interface WithHtmlPattern : UrlConverter {
        val conversionHtmlPattern: ConversionHtmlPattern<PositionRegex>?
        val conversionHtmlRedirectPattern: ConversionHtmlPattern<RedirectRegex>?
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }
}
