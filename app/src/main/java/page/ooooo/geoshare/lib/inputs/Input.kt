package page.ooooo.geoshare.lib.inputs

import androidx.compose.runtime.Composable
import com.google.re2j.Pattern
import kotlinx.io.Source
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.position.Position

interface Input {

    data class Documentation(val nameResId: Int, val inputs: List<DocumentationInput>)

    sealed class DocumentationInput(val addedInVersionCode: Int) {
        class Text(addedInVersionCode: Int, val text: @Composable () -> String) : DocumentationInput(addedInVersionCode)
        class Url(addedInVersionCode: Int, val urlString: String) : DocumentationInput(addedInVersionCode)
    }

    enum class ShortUriMethod { GET, HEAD }

    val uriPattern: Pattern
    val documentation: Documentation

    fun parseUri(uri: Uri): Pair<Position, String?>

    interface HasShortUri : Input {
        val shortUriPattern: Pattern
        val shortUriMethod: ShortUriMethod
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int
    }

    interface HasHtml : Input {
        val permissionTitleResId: Int
        val loadingIndicatorTitleResId: Int

        fun parseHtml(source: Source): Pair<Position, String?>
    }
}
