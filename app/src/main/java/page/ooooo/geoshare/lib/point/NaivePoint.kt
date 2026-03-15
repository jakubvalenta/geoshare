package page.ooooo.geoshare.lib.point

import androidx.compose.runtime.Immutable

/**
 * Point without spatial reference system (SRS) information.
 *
 * It should be used only as an intermediary data structure during input processing. It must be converted to a point
 * with SRS information, such a [WGS84Point] or [GCJ02Point], before it can be used to open and app or copy coordinates.
 */
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
