package page.ooooo.geoshare.lib

import androidx.compose.runtime.Immutable
import kotlin.random.Random

@Immutable
data class Point(val lat: String = "0", val lon: String = "0", val desc: String? = null) {
    companion object {
        val example: Point = genRandomPoint(minLat = 0.0, maxLon = -100.0)

        fun genRandomPoint(
            minLat: Double = -50.0,
            maxLat: Double = 80.0,
            minLon: Double = -180.0,
            maxLon: Double = 180.0,
        ): Point = Point(
            Random.nextDouble(minLat, maxLat).toScale(6).toString(),
            Random.nextDouble(minLon, maxLon).toScale(6).toString(),
        )
    }
}
