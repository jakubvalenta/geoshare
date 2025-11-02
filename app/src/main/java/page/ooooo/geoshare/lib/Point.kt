package page.ooooo.geoshare.lib

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableMap
import kotlin.math.abs
import kotlin.random.Random

@Immutable
data class Point(val lat: String = "0", val lon: String = "0") {
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

    fun toCoordsDecString(): String = "$lat, $lon"

    fun toGeoUriString(q: String? = null, z: String? = null, uriQuote: UriQuote = DefaultUriQuote()): String = Uri(
        scheme = "geo",
        path = "$lat,$lon",
        queryParams = buildMap {
            set("q", q ?: "$lat,$lon")
            z?.let { z ->
                set("z", z)
            }
        }.toImmutableMap(),
        uriQuote = uriQuote,
    ).toString()

    fun toDegMinSecCoordsString(): String =
        (lat.toDoubleOrNull() ?: 0.0).toDegMinSec().let { (deg, min, sec) ->
            "${abs(deg)}°\u00a0$min′\u00a0${sec.toScale(5)}″\u00a0${if (deg < 0) "S" else "N"}, "
        } +
                (lon.toDoubleOrNull() ?: 0.0).toDegMinSec().let { (deg, min, sec) ->
                    "${abs(deg)}°\u00a0$min′\u00a0${sec.toScale(5)}″\u00a0${if (deg < 0) "W" else "E"}"
                }
}
