package page.ooooo.geoshare.lib.formatters

import page.ooooo.geoshare.lib.extensions.toDegMinSec
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class CoordinateFormatter @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
) {
    fun formatDecCoords(point: Point) = coordinateConverter.toWGS84(point).run {
        latStr?.let { latStr ->
            lonStr?.let { lonStr ->
                "$latStr, $lonStr"
            }
        } ?: "0, 0"
    }

    fun formatDegMinSecCoords(point: Point): String = coordinateConverter.toWGS84(point).run {
        lat?.let { lat ->
            lon?.let { lon ->
                lat.toDegMinSec().let { (deg, min, sec) ->
                    "${abs(deg)}°\u00a0$min′\u00a0${sec.toScale(5)}″\u00a0${if (deg < 0) "S" else "N"}, "
                } + lon.toDegMinSec().let { (deg, min, sec) ->
                    "${abs(deg)}°\u00a0$min′\u00a0${sec.toScale(5)}″\u00a0${if (deg < 0) "W" else "E"}"
                }
            }
        } ?: "0 E, 0 N"
    }
}
