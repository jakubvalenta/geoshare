package page.ooooo.geoshare.lib.android

@Suppress("SpellCheckingInspection")
object PackageNames {
    const val AMAP = "com.autonavi.minimap"
    const val GMAPS_WV = "us.spotco.maps"
    const val GOOGLE_MAPS = "com.google.android.apps.maps"
    const val TOMTOM = "com.tomtom.speedcams.android.map"
    const val GEO_SHARE = "page.ooooo.geoshare"
    const val GEO_SHARE_DEBUG = "page.ooooo.geoshare.debug"

    val GOOGLE_MAPS_LIKE = setOf(
        GOOGLE_MAPS,
        GMAPS_WV,
    )
    val GCJ02 = GOOGLE_MAPS_LIKE + AMAP
}
