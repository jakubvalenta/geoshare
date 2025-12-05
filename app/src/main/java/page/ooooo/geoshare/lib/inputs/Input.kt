package page.ooooo.geoshare.lib.inputs

import androidx.compose.runtime.Composable
import com.google.re2j.Pattern
import io.ktor.utils.io.ByteReadChannel
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.position.Position

interface Input {

    enum class DocumentationId {
        AMAP,
        APPLE_MAPS,
        COORDINATES,
        GEO_URI,
        GOOGLE_MAPS,
        HERE_WEGO,
        MAGIC_EARTH,
        MAPS_ME,
        MAPY_COM,
        OPEN_STREET_MAP,
        OSM_AND,
        URBI,
        WAZE,
        YANDEX_MAPS,
    }

    data class Documentation(val id: DocumentationId, val nameResId: Int, val inputs: List<DocumentationInput>)

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

        suspend fun parseHtml(channel: ByteReadChannel): Pair<Position, String?>
    }
}
