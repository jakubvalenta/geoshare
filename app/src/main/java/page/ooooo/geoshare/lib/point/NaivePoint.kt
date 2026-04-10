package page.ooooo.geoshare.lib.point

import androidx.compose.runtime.Immutable

/**
 * Point without spatial reference system (SRS) information.
 *
 * It must be used only as an intermediary data structure during input processing. It must be converted to a point with
 * SRS information, such a [WGS84Point] or [GCJ02Point], before it can be used to open an app or copy coordinates.
 */
@Immutable
data class NaivePoint(
    val lat: Double? = null,
    val lon: Double? = null,
    val z: Double? = null,
    val name: String? = null,
    val source: Source,
) {
    fun hasCoordinates(): Boolean = lat != null && lon != null
}
