package page.ooooo.geoshare.lib

import androidx.compose.runtime.Immutable
import com.lbt05.evil_transform.TransformUtil
import com.lbt05.evil_transform.WGSPointer
import kotlin.random.Random

@Immutable
data class Point(val lat: Double = 0.0, val lon: Double = 0.0, val desc: String? = null) {
    companion object {
        val example: Point = genRandomPoint(minLat = 0.0, maxLon = -100.0)

        fun genRandomPoint(
            minLat: Double = -50.0,
            maxLat: Double = 80.0,
            minLon: Double = -180.0,
            maxLon: Double = 180.0,
        ): Point = Point(
            Random.nextDouble(minLat, maxLat).toScale(6),
            Random.nextDouble(minLon, maxLon).toScale(6),
        )
    }

    fun isOutOfChina(): Boolean = TransformUtil.outOfChina(lat, lon)

    fun toGCJ(): Point = WGSPointer(lat, lon).toGCJPointer().run { Point(latitude, longitude) }

    fun toStringPair(): Pair<String, String> = lat.toScale(7).toString() to lon.toScale(7).toString()
}
