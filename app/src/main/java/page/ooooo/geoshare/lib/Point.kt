package page.ooooo.geoshare.lib

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableMap
import kotlin.math.abs

@Immutable
data class Point(val lat: String = "0", val lon: String = "0") {
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
