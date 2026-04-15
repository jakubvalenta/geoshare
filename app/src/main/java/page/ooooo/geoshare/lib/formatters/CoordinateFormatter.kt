package page.ooooo.geoshare.lib.formatters

import page.ooooo.geoshare.lib.extensions.toDegMinSec
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.geo.WGS84Point
import kotlin.math.abs

object CoordinateFormatter {
    fun formatDecCoords(point: WGS84Point) = point.run {
        latStr?.let { latStr ->
            lonStr?.let { lonStr ->
                "$latStr, $lonStr"
            }
        } ?: "0, 0"
    }

    fun formatDegMinSecCoords(point: WGS84Point): String = point.run {
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
