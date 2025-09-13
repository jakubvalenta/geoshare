package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.ConversionHtmlPattern
import page.ooooo.geoshare.lib.ConversionUrlPattern

interface UrlConverter {
    val name: String
    val host: Pattern
    val shortUrlPattern: Pattern?
    val urlPattern: ConversionUrlPattern
    val htmlPattern: ConversionHtmlPattern?
    val htmlRedirectPattern: ConversionHtmlPattern?
    val permissionTitleResId: Int
    val loadingIndicatorTitleResId: Int
}
