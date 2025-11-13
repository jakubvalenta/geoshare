package page.ooooo.geoshare.lib.inputs

import androidx.compose.runtime.Composable
import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.conversion.ConversionPattern

sealed interface Input {

    data class Documentation(val nameResId: Int, val inputs: List<DocumentationInput>)

    sealed class DocumentationInput(val addedInVersionCode: Int) {
        class Text(addedInVersionCode: Int, val text: @Composable () -> String) : DocumentationInput(addedInVersionCode)
        class Url(addedInVersionCode: Int, val urlString: String) : DocumentationInput(addedInVersionCode)
    }

    enum class ShortUriMethod { GET, HEAD }

    val uriPattern: Pattern
    val documentation: Documentation
    val conversionUriPattern: ConversionPattern.UriPattern

    interface HasShortUri : Input {
        val shortUriPattern: Pattern
        val shortUriMethod: ShortUriMethod
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }

    interface HasHtml : Input {
        val conversionHtmlPattern: ConversionPattern.HtmlPattern
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }
}
