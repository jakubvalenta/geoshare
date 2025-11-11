package page.ooooo.geoshare.lib

import com.google.re2j.Matcher
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.lastNotNullOrNull
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import kotlin.math.max
import kotlin.math.min

sealed interface PositionMatch {
    companion object {
        const val MAX_COORD_PRECISION = 17
        const val LAT_NUM = """-?\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?"""
        const val LON_NUM = """-?\d{1,3}(\.\d{1,$MAX_COORD_PRECISION})?"""
        const val LAT = """[\+ ]?(?P<lat>$LAT_NUM)"""
        const val LON = """[\+ ]?(?P<lon>$LON_NUM)"""
        const val Z = """(?P<z>\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?)"""
        const val Q_PARAM = """(?P<q>.+)"""
        const val Q_PATH = """(?P<q>[^/]+)"""
    }
}

data class IncompletePosition(
    val srs: Srs,
    val lat: Double? = null,
    val lon: Double? = null,
    val q: String? = null,
    val z: Double? = null,
)

class IncompletePoint {
    var srs: Srs? = null
    var lat: Double? = null
    var lon: Double? = null

    fun read(): Point? = srs?.let { srs ->
        lat?.let { lat ->
            lon?.let { lon ->
                this.srs = null
                this.lat = null
                this.lon = null
                Point(srs, lat, lon)
            }
        }
    }
}

fun Matcher.toIncompleteLatLonPosition(srs: Srs): IncompletePosition? =
    this.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
        this.groupOrNull("lon")?.toDoubleOrNull()?.let { lon ->
            IncompletePosition(srs, lat = lat, lon = lon)
        }
    }

fun Matcher.toIncompleteLatLonZPosition(srs: Srs): IncompletePosition? =
    this.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
        this.groupOrNull("lon")?.toDoubleOrNull()?.let { lon ->
            this.groupOrNull("z")?.toDoubleOrNull()?.let { z ->
                IncompletePosition(srs, lat = lat, lon = lon, z = z)
            }
        }
    }

fun Matcher.toIncompleteLatPosition(srs: Srs): IncompletePosition? =
    this.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
        IncompletePosition(srs, lat = lat)
    }

fun Matcher.toIncompleteLonPosition(srs: Srs): IncompletePosition? =
    this.groupOrNull("lon")?.toDoubleOrNull()?.let { lon ->
        IncompletePosition(srs, lon = lon)
    }

fun Matcher.toIncompleteQPosition(srs: Srs): IncompletePosition? =
    this.groupOrNull("q")?.let { q ->
        IncompletePosition(srs, q = q)
    }

fun Matcher.toIncompleteZPosition(srs: Srs): IncompletePosition? =
    this.groupOrNull("z")?.toDoubleOrNull()?.let {
        IncompletePosition(srs, z = max(1.0, min(21.0, it)))
    }

fun Sequence<IncompletePosition>.toPosition(): Position {
    val points: MutableList<Point> = mutableListOf()
    val incompletePoint = IncompletePoint()
    var q: String? = null
    var z: Double? = null
    for (incompletePosition in this) {
        if (incompletePosition.lat != null && incompletePosition.lon != null) {
            points.add(Point(incompletePosition.srs, incompletePosition.lat, incompletePosition.lon))
        } else if (incompletePosition.lat != null) {
            incompletePoint.srs = incompletePosition.srs
            incompletePoint.lat = incompletePosition.lat
            incompletePoint.read()?.let { point ->
                points.add(point)
            }
        } else if (incompletePosition.lon != null) {
            incompletePoint.srs = incompletePosition.srs
            incompletePoint.lon = incompletePosition.lon
            incompletePoint.read()?.let { point ->
                points.add(point)
            }
        }
        if (incompletePosition.q != null) {
            q = incompletePosition.q
        }
        if (incompletePosition.z != null) {
            z = incompletePosition.z
        }
    }
    return Position(points.toImmutableList(), q = q, z = z)
}

open class RedirectMatch(val matcher: Matcher) {
    open val url: String? get() = matcher.groupOrNull("url")
}

fun List<RedirectMatch>.toUrlString() = this.lastNotNullOrNull { it.url }
