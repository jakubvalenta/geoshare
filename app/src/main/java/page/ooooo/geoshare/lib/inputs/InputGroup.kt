package page.ooooo.geoshare.lib.inputs

import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import page.ooooo.geoshare.R

@Keep
enum class InputGroupId {
    AMAP,
    APPLE_MAPS,
    BAIDU_MAP,
    CARTES_IGN,
    COORDINATES,
    DEBUG,
    GEO_URI,
    GOOGLE_MAPS,
    GOOGLE_NAVIGATION_URI,
    HERE_WEGO,
    MAGIC_EARTH,
    MAPS_ME,
    MAPY_COM,
    OPEN_STREET_MAP,
    OSM_AND,
    PLUS_CODE,
    URBI,
    WAZE,
    YANDEX_MAPS,
}

@Immutable
data class InputGroup(val id: InputGroupId, @param:StringRes val nameResId: Int) {
    companion object {
        val AMAP = InputGroup(InputGroupId.AMAP, R.string.converter_amap_name)
        val APPLE_MAPS = InputGroup(InputGroupId.APPLE_MAPS, R.string.converter_apple_maps_name)
        val BAIDU_MAP = InputGroup(InputGroupId.BAIDU_MAP, R.string.converter_baidu_map_name)
        val CARTES_IGN = InputGroup(InputGroupId.CARTES_IGN, R.string.converter_cartes_ign_name)
        val COORDINATES = InputGroup(InputGroupId.COORDINATES, R.string.converter_coordinates_name)
        val DEBUG = InputGroup(InputGroupId.DEBUG, R.string.converter_debug_name)
        val GEO_URI = InputGroup(InputGroupId.GEO_URI, R.string.converter_geo_name)
        val GOOGLE_MAPS = InputGroup(InputGroupId.GOOGLE_MAPS, R.string.converter_google_maps_name)
        val GOOGLE_NAVIGATION_URI =
            InputGroup(InputGroupId.GOOGLE_NAVIGATION_URI, R.string.converter_google_navigation_uri_name)
        val HERE_WEGO = InputGroup(InputGroupId.HERE_WEGO, R.string.converter_here_wego_name)
        val MAGIC_EARTH = InputGroup(InputGroupId.MAGIC_EARTH, R.string.converter_magic_earth_name)
        val MAPS_ME = InputGroup(InputGroupId.MAPS_ME, R.string.converter_ge0_name)
        val MAPY_COM = InputGroup(InputGroupId.MAPY_COM, R.string.converter_mapy_com_name)
        val OPEN_STREET_MAP = InputGroup(InputGroupId.OPEN_STREET_MAP, R.string.converter_open_street_map_name)
        val OSM_AND = InputGroup(InputGroupId.OSM_AND, R.string.converter_osm_and_name)
        val PLUS_CODE = InputGroup(InputGroupId.PLUS_CODE, R.string.converter_plus_code_name)
        val URBI = InputGroup(InputGroupId.URBI, R.string.converter_urbi_name)
        val WAZE = InputGroup(InputGroupId.WAZE, R.string.converter_waze_name)
        val YANDEX_MAPS = InputGroup(InputGroupId.YANDEX_MAPS, R.string.converter_yandex_maps_name)
    }
}
