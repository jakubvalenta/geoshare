package page.ooooo.geoshare.lib.converters

import androidx.compose.runtime.Composable
import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.*
import java.net.URL

data class Documentation(val nameResId: Int, val inputs: List<DocumentationInput>)

sealed class DocumentationInput(val addedInVersionCode: Int) {
    class Text(addedInVersionCode: Int, val text: @Composable () -> String) :
        DocumentationInput(addedInVersionCode)

    class Url(addedInVersionCode: Int, val urlString: String) : DocumentationInput(addedInVersionCode)
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
