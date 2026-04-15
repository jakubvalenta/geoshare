@file:Suppress("SpellCheckingInspection")

package page.ooooo.geoshare.lib.android

import page.ooooo.geoshare.lib.formatters.GeoUriFlavor
import page.ooooo.geoshare.lib.geo.Srs

object PackageNames {
    const val AMAP = "com.autonavi.minimap"
    const val BAIDU_MAP = "com.baidu.BaiduMap"
    const val COMAPS_FDROID = "app.comaps.fdroid"
    const val COMAPS_PREFIX = "app.comaps."
    const val GARMIN_PREFIX = "com.garmin."
    const val GARMIN_EXPLORE = "${GARMIN_PREFIX}android.apps.explore"
    const val GMAPS_WV = "us.spotco.maps"
    const val GOOGLE_MAPS = "com.google.android.apps.maps"
    const val HERE_WEGO = "com.here.app.maps"
    const val KOMOOT = "de.komoot.android"
    const val LOCUS_MAP = "menion.android.locus"
    const val MAGIC_EARTH = "com.generalmagic.magicearth"
    const val MAPS_ME = "com.mapswithme.maps.pro"
    const val MAPY_COM = "cz.seznam.mapy"
    const val OEFFI = "de.schildbach.oeffi"
    const val ORGANIC_MAPS = "app.organicmaps"
    const val OSMAND = "net.osmand"
    const val OSMAND_PLUS = "net.osmand.plus"
    const val SYGIC = "com.sygic.aura"
    const val TEST = "com.example.test"
    const val TOMTOM_PREFIX = "com.tomtom."
    const val TOMTOM = "${TOMTOM_PREFIX}speedcams.android.map"
    const val VESPUCCI = "de.blau.android"

    fun getSrs(packageName: String?): Srs =
        when (packageName) {
            AMAP -> Srs.GCJ02_GREATER_CHINA_AND_TAIWAN
            GOOGLE_MAPS, GMAPS_WV -> Srs.GCJ02_MAINLAND_CHINA
            // Notice that Baidu Map uses WGS 84 geo: URIs, although all its other links are in BD09MC
            else -> Srs.WGS84
        }

    fun getGeoUriFlavor(packageName: String?): GeoUriFlavor =
        when {
            packageName == AMAP ||
                packageName == HERE_WEGO ||
                packageName == GOOGLE_MAPS ||
                packageName == GMAPS_WV ||
                packageName == MAPS_ME ||
                packageName == MAPY_COM ||
                packageName == ORGANIC_MAPS ||
                packageName == OSMAND ||
                packageName == OSMAND_PLUS ||
                packageName == SYGIC ||
                packageName == VESPUCCI ||
                packageName?.startsWith(COMAPS_PREFIX) == true ->
                GeoUriFlavor.Best

            packageName == BAIDU_MAP ->
                GeoUriFlavor(pin = GeoUriFlavor.PinFlavor.NOT_AVAILABLE, zoom = GeoUriFlavor.ZoomFlavor.ALONE_ONLY)

            packageName?.startsWith(GARMIN_PREFIX) == true ->
                GeoUriFlavor(
                    pin = GeoUriFlavor.PinFlavor.COORDS_AND_NAME_IN_Q,
                    zoom = GeoUriFlavor.ZoomFlavor.NOT_AVAILABLE
                )

            packageName == KOMOOT ->
                GeoUriFlavor(pin = GeoUriFlavor.PinFlavor.COORDS_ONLY_IN_Q, zoom = GeoUriFlavor.ZoomFlavor.ANY)

            packageName == LOCUS_MAP ->
                GeoUriFlavor(pin = GeoUriFlavor.PinFlavor.NAME_ONLY_IN_Q, zoom = GeoUriFlavor.ZoomFlavor.ANY)

            packageName == OEFFI ->
                GeoUriFlavor(pin = GeoUriFlavor.PinFlavor.COORDS_ONLY_IN_Q, zoom = GeoUriFlavor.ZoomFlavor.ANY)

            else ->
                GeoUriFlavor.Safe
        }
}
