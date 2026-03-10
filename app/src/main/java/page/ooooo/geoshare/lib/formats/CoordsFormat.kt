package page.ooooo.geoshare.lib.formats

import page.ooooo.geoshare.lib.extensions.toDegMinSec
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.point.Point
import kotlin.math.abs

object CoordsFormat {
    fun formatDecCoords(point: Point) = point.toWGS84().run {
        latStr?.let { latStr ->
            lonStr?.let { lonStr ->
                "$latStr, $lonStr"
            }
        } ?: "0, 0"
    }

    fun formatDegMinSecCoords(point: Point): String = point.toWGS84().run {
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
