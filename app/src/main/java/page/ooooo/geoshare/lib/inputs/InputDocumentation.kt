package page.ooooo.geoshare.lib.inputs

import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import page.ooooo.geoshare.R

data class InputDocumentation(val group: InputDocumentationGroup, val items: List<InputDocumentationItem>)

@Keep
enum class InputDocumentationGroup(@param:StringRes val nameResId: Int) {
    AMAP(R.string.converter_amap_name),
    APPLE_MAPS(R.string.converter_apple_maps_name),
    BAIDU_MAP(R.string.converter_baidu_map_name),
    CARTES_IGN(R.string.converter_cartes_ign_name),
    COORDINATES(R.string.converter_coordinates_name),
    DEBUG(R.string.converter_debug_name),
    GEO_URI(R.string.converter_geo_name),
    GOOGLE_MAPS(R.string.converter_google_maps_name),
    HERE_WEGO(R.string.converter_here_wego_name),
    MAGIC_EARTH(R.string.converter_magic_earth_name),
    MAPS_ME(R.string.converter_ge0_name),
    MAPY_COM(R.string.converter_mapy_com_name),
    OPEN_STREET_MAP(R.string.converter_open_street_map_name),
    OSM_AND(R.string.converter_osm_and_name),
    PLUS_CODE(R.string.converter_plus_code_name),
    URBI(R.string.converter_urbi_name),
    WAZE(R.string.converter_waze_name),
    YANDEX_MAPS(R.string.converter_yandex_maps_name),
}

sealed class InputDocumentationItem(val addedInVersionCode: Int) {
    class Text(addedInVersionCode: Int, val text: @Composable () -> String) : InputDocumentationItem(addedInVersionCode)
    class Url(addedInVersionCode: Int, val urlString: String) : InputDocumentationItem(addedInVersionCode)
}
