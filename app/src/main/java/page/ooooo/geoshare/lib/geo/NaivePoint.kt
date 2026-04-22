package page.ooooo.geoshare.lib.geo

import androidx.compose.runtime.Immutable
import page.ooooo.geoshare.lib.extensions.toScale
import kotlin.random.Random

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

    companion object {
        val example = genRandomPoint(minLat = 0.0, maxLon = -100.0)

        fun genRandomPoint(
            minLat: Double = -50.0,
            maxLat: Double = 80.0,
            minLon: Double = -180.0,
            maxLon: Double = 180.0,
            z: Double = 16.0,
            name: String? = null,
            source: Source = Source.GENERATED,
        ): NaivePoint = NaivePoint(
            Random.nextDouble(minLat, maxLat).toScale(6),
            Random.nextDouble(minLon, maxLon).toScale(6),
            z, name, source,
        )
    }
}
