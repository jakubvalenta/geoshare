package page.ooooo.geoshare.lib.position

import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.extensions.groupOrNull
import kotlin.math.max
import kotlin.math.min
import kotlin.sequences.mapNotNull

const val MAX_COORD_PRECISION = 17
const val LAT_NUM = """-?\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?"""
const val LON_NUM = """-?\d{1,3}(\.\d{1,$MAX_COORD_PRECISION})?"""
const val LAT = """[\+ ]?(?P<lat>$LAT_NUM)"""
const val LON = """[\+ ]?(?P<lon>$LON_NUM)"""
const val Z = """(?P<z>\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?)"""
const val Q_PARAM = """(?P<q>.+)"""
const val Q_PATH = """(?P<q>[^/]+)"""

val LAT_PATTERN: Pattern = Pattern.compile(LAT)
val LON_PATTERN: Pattern = Pattern.compile(LON)
val LAT_LON_PATTERN: Pattern = Pattern.compile("$LAT,$LON")
val LON_LAT_PATTERN: Pattern = Pattern.compile("$LON,$LAT")
val Z_PATTERN: Pattern = Pattern.compile(Z)
val Q_PARAM_PATTERN: Pattern = Pattern.compile(Q_PARAM)

class PositionBuilder(val srs: Srs) {

    var points: MutableList<Point> = mutableListOf()
    var defaultPoint: Point? = null
    var q: String? = null
    var z: Double? = null
    var uriString: String? = null

    val position: Position
        get() = Position(
            points.takeIf { it.isNotEmpty() }?.toImmutableList()
                ?: defaultPoint?.let { persistentListOf(it) },
            q = q,
            z = z,
        )

    fun toPair(): Pair<Position, String?> = position to uriString

    fun setPointFromMatcher(block: () -> Matcher?) {
        if (points.isEmpty()) {
            block()?.toPoint(srs)?.let {
                points.add(it)
            }
        }
    }

    fun setPointAndZoomFromMatcher(block: () -> Matcher?) {
        if (points.isEmpty()) {
            block()?.toPointAndZ(srs)?.let { (point, newZ) ->
                points.add(point)
                z = newZ
            }
        }
    }

    fun setLatLon(block: () -> Pair<Double, Double>?) {
        if (points.isEmpty()) {
            block()?.let { (lat, lon) ->
                points.add(Point(srs, lat, lon))
            }
        }
    }

    fun setLatLonZoom(block: () -> Triple<Double, Double, Double>?) {
        if (points.isEmpty()) {
            block()?.let { (lat, lon, newZ) ->
                points.add(Point(srs, lat, lon))
                z = newZ
            }
        }
    }

    fun addPointsFromSequenceOfMatchers(block: () -> Sequence<Matcher>) {
        points.addAll(block().mapNotNull { m -> m.toPoint(srs) })
    }

    fun setDefaultPointFromMatcher(block: () -> Matcher?) {
        if (defaultPoint == null) {
            block()?.toPoint(srs)?.let {
                defaultPoint = it
            }
        }
    }

    fun setQueryFromMatcher(block: () -> Matcher?) {
        if (points.isEmpty() && q == null) {
            q = block()?.toQ()
        }
    }

    fun setZoomFromMatcher(block: () -> Matcher?) {
        if (z == null) {
            z = block()?.toZ()
        }
    }

    fun setUriString(block: () -> String?) {
        if (uriString == null) {
            uriString = block()
        }
    }

    fun setUriStringFromMatcher(block: () -> Matcher?) {
        if (uriString == null) {
            block()?.toUriString()?.let {
                uriString = it
            }
        }
    }
}

fun Matcher.toPoint(srs: Srs): Point? =
    this.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
        this.groupOrNull("lon")?.toDoubleOrNull()?.let { lon ->
            Point(srs, lat, lon)
        }
    }

fun Matcher.toPointAndZ(srs: Srs): Pair<Point, Double>? =
    this.toPoint(srs)?.let { point ->
        this.toZ()?.let { z ->
            point to z
        }
    }

fun Matcher.toQ(): String? =
    this.groupOrNull("q")

fun Matcher.toZ(): Double? =
    this.groupOrNull("z")?.toDoubleOrNull()?.let { z ->
        max(1.0, min(21.0, z))
    }

fun Matcher.toUriString(): String? =
    this.groupOrNull("url")
