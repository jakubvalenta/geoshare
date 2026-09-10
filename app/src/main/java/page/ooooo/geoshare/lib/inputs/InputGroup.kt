package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import androidx.annotation.Keep
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

interface InputGroup {
    val id: InputGroupId
    fun getName(resources: Resources): String

    companion object {
        val AMAP = object : InputGroup {
            override val id = InputGroupId.AMAP
            override fun getName(resources: Resources) =
                resources.getString(R.string.converter_amap_name)
        }
        val APPLE_MAPS = object : InputGroup {
            override val id = InputGroupId.APPLE_MAPS
            override fun getName(resources: Resources) =
                resources.getString(R.string.converter_apple_maps_name)
        }
        val BAIDU_MAP = object : InputGroup {
            override val id = InputGroupId.BAIDU_MAP
            override fun getName(resources: Resources) =
                resources.getString(R.string.converter_baidu_map_name)
        }
        val CARTES_IGN = object : InputGroup {
            override val id = InputGroupId.CARTES_IGN
            override fun getName(resources: Resources) =
                resources.getString(R.string.converter_cartes_ign_name)
        }
        val COORDINATES = object : InputGroup {
            override val id = InputGroupId.COORDINATES
            override fun getName(resources: Resources) =
                resources.getString(R.string.converter_coordinates_name)
        }
        val DEBUG = object : InputGroup {
            override val id = InputGroupId.DEBUG
            override fun getName(resources: Resources) = "Debug Input"
        }
        val GEO_URI = object : InputGroup {
            override val id = InputGroupId.GEO_URI
            override fun getName(resources: Resources) =
                resources.getString(R.string.converter_geo_name)
        }
        val GOOGLE_MAPS = object : InputGroup {
            override val id = InputGroupId.GOOGLE_MAPS
            override fun getName(resources: Resources) =
                resources.getString(R.string.converter_google_maps_name)
        }
        val GOOGLE_NAVIGATION_URI =
            object : InputGroup {
                override val id = InputGroupId.GOOGLE_NAVIGATION_URI
                override fun getName(resources: Resources) =
                    resources.getString(R.string.converter_google_navigation_uri_name)
            }
        val HERE_WEGO = object : InputGroup {
            override val id = InputGroupId.HERE_WEGO
            override fun getName(resources: Resources) =
                resources.getString(R.string.converter_here_wego_name)
        }
        val MAGIC_EARTH = object : InputGroup {
            override val id = InputGroupId.MAGIC_EARTH
            override fun getName(resources: Resources) =
                resources.getString(R.string.converter_magic_earth_name)
        }
        val MAPS_ME = object : InputGroup {
            override val id = InputGroupId.MAPS_ME
            override fun getName(resources: Resources) =
                resources.getString(R.string.converter_ge0_name)
        }
        val MAPY_COM = object : InputGroup {
            override val id = InputGroupId.MAPY_COM
            override fun getName(resources: Resources) =
                resources.getString(R.string.converter_mapy_com_name)
        }
        val OPEN_STREET_MAP = object : InputGroup {
            override val id = InputGroupId.OPEN_STREET_MAP
            override fun getName(resources: Resources) =
                resources.getString(R.string.converter_open_street_map_name)
        }
        val OSM_AND = object : InputGroup {
            override val id = InputGroupId.OSM_AND
            override fun getName(resources: Resources) =
                resources.getString(R.string.converter_osm_and_name)
        }
        val PLUS_CODE = object : InputGroup {
            override val id = InputGroupId.PLUS_CODE
            override fun getName(resources: Resources) =
                resources.getString(R.string.converter_plus_code_name)
        }
        val URBI = object : InputGroup {
            override val id = InputGroupId.URBI
            override fun getName(resources: Resources) =
                resources.getString(R.string.converter_urbi_name)
        }
        val WAZE = object : InputGroup {
            override val id = InputGroupId.WAZE
            override fun getName(resources: Resources) =
                resources.getString(R.string.converter_waze_name)
        }
        val YANDEX_MAPS = object : InputGroup {
            override val id = InputGroupId.YANDEX_MAPS
            override fun getName(resources: Resources) =
                resources.getString(R.string.converter_yandex_maps_name)
        }
    }
}
