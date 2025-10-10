package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.*
import java.net.URL

data class Documentation(val nameResId: Int, val inputs: List<DocumentationInput>)

sealed class DocumentationInput(val addedInVersionCode: Int) {
    class Text(val descriptionResId: Int, addedInVersionCode: Int) : DocumentationInput(addedInVersionCode)
    class Url(val urlString: String, addedInVersionCode: Int) : DocumentationInput(addedInVersionCode)
}

enum class ShortUriMethod { GET, HEAD }

sealed interface UrlConverter {
    val uriPattern: Pattern
    val documentation: Documentation

    interface WithShortUriPattern : UrlConverter {
        val shortUriPattern: Pattern
        val shortUriMethod: ShortUriMethod
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }

    interface WithUriPattern : UrlConverter {
        val conversionUriPattern: ConversionUriPattern<PositionRegex>
    }

    interface WithHtmlPattern : UrlConverter {
        val conversionHtmlPattern: ConversionHtmlPattern<PositionRegex>?
        val conversionHtmlRedirectPattern: ConversionHtmlPattern<RedirectRegex>?
        fun getHtmlUrl(uri: Uri): URL? = uri.toUrl()
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }
}
