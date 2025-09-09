package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.ConversionHtmlPattern
import page.ooooo.geoshare.lib.ConversionUriPattern

interface UrlConverter {
    val name: String
    val uriPattern: Pattern
    val shortUriPattern: Pattern?
    val conversionUriPattern: ConversionUriPattern
    val conversionHtmlPattern: ConversionHtmlPattern?
    val conversionHtmlRedirectPattern: ConversionHtmlPattern?
    val permissionTitleResId: Int
    val loadingIndicatorTitleResId: Int
}
