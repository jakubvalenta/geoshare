package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.ConversionHtmlPattern
import page.ooooo.geoshare.lib.ConversionUriPattern

interface UrlConverter {
    val name: String
    val uriPattern: Pattern
    val conversionUriPattern: ConversionUriPattern
}

interface HasShortUri {
    val shortUriPattern: Pattern?
    val permissionTitleResId: Int
    val loadingIndicatorTitleResId: Int
}

interface HasHtmlPattern {
    val conversionHtmlPattern: ConversionHtmlPattern?
    val permissionTitleResId: Int
    val loadingIndicatorTitleResId: Int
}

interface HasHtmlRedirectPattern {
    val conversionHtmlRedirectPattern: ConversionHtmlPattern?
    val permissionTitleResId: Int
    val loadingIndicatorTitleResId: Int
}
