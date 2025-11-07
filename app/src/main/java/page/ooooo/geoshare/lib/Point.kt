package page.ooooo.geoshare.lib

import androidx.compose.runtime.Immutable
import com.lbt05.evil_transform.GCJPointer
import com.lbt05.evil_transform.TransformUtil
import com.lbt05.evil_transform.WGSPointer
import page.ooooo.geoshare.lib.extensions.toScale
import kotlin.random.Random

@Immutable
data class Point(val srs: Srs, val lat: Double = 0.0, val lon: Double = 0.0, val desc: String? = null) {
    companion object {
        val example: Point = genRandomPoint(minLat = 0.0, maxLon = -100.0)

        fun genRandomPoint(
            minLat: Double = -50.0,
            maxLat: Double = 80.0,
            minLon: Double = -180.0,
            maxLon: Double = 180.0,
        ): Point {
            val lat = Random.nextDouble(minLat, maxLat).toScale(6)
            val lon = Random.nextDouble(minLon, maxLon).toScale(6)
            val srs = if (TransformUtil.outOfChina(lat, lon)) Srs.WGS84 else Srs.GCJ02
            return Point(srs, lat, lon)
        }
    }

    fun toStringPair(targetSrs: Srs): Pair<String, String> = toSrs(targetSrs).let { (_, lat, lon) ->
        lat.toScale(7).toString() to lon.toScale(7).toString()
    }

    fun toSrs(targetSrs: Srs): Point = when (srs) {
        is Srs.WGS84 -> when (targetSrs) {
            is Srs.WGS84 -> this
            is Srs.GCJ02 -> WGSPointer(lat, lon).toGCJPointer().run { Point(targetSrs, latitude, longitude) }
        }

        is Srs.GCJ02 -> when (targetSrs) {
            is Srs.WGS84 -> GCJPointer(lat, lon).toExactWGSPointer().run { Point(targetSrs, latitude, longitude) }
            is Srs.GCJ02 -> this
        }
    }
}
