package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern

interface UrlConverter {
    val name: String
    val host: Pattern
    val shortUrlHost: Pattern?
    val urlPattern: UrlPattern
    val htmlPattern: HtmlPattern?
    val htmlRedirectPattern: HtmlPattern?
    val permissionTitleResId: Int
    val loadingIndicatorTitleResId: Int
}
