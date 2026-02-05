package page.ooooo.geoshare.lib.inputs

import androidx.annotation.Keep
import androidx.compose.runtime.Composable

@Keep
enum class InputDocumentationId {
    AMAP,
    APPLE_MAPS,
    BAIDU_MAP,
    COORDINATES,
    DEBUG,
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

data class InputDocumentation(val id: InputDocumentationId, val nameResId: Int, val items: List<InputDocumentationItem>)

sealed class InputDocumentationItem(val addedInVersionCode: Int) {
    class Text(addedInVersionCode: Int, val text: @Composable () -> String) : InputDocumentationItem(addedInVersionCode)
    class Url(addedInVersionCode: Int, val urlString: String) : InputDocumentationItem(addedInVersionCode)
}
