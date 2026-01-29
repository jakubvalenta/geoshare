package page.ooooo.geoshare.lib.point

import androidx.compose.runtime.Immutable

@Immutable
data class NaivePoint(
    val lat: Double? = null,
    val lon: Double? = null,
    val z: Double? = null,
    val name: String? = null,
) {
    fun asWGS84() = WGS84Point(lat, lon, z, name)
    fun asGCJ02() = GCJ02Point(lat, lon, z, name)
    fun asBD09MC() = BD09MCPoint(lat, lon, z, name)
}
