package page.ooooo.geoshare.lib

import com.google.re2j.Matcher
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.max
import kotlin.math.min

open class PositionMatch(val matcher: Matcher) {
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

    open val points: List<Point>?
        get() = lat?.let { lat -> lon?.let { lon -> persistentListOf(Point(lat, lon)) } }
    open val lat: String?
        get() = matcher.groupOrNull("lat")
    open val lon: String?
        get() = matcher.groupOrNull("lon")
    open val q: String?
        get() = matcher.groupOrNull("q")
    open val z: String?
        get() = matcher.groupOrNull("z")?.toDouble()?.let { max(1.0, min(21.0, it)) }?.toTrimmedString()
}

/**
 * Repeatedly searches for LAT and LON in the input to get points.
 */
class PointsPositionMatch(matcher: Matcher) : PositionMatch(matcher) {
    override val points: List<Point>
        get() = matcher.reset().let { m ->
            buildList {
                while (m.find()) {
                    try {
                        add(Point(m.group("lat"), m.group("lon")))
                    } catch (_: IllegalArgumentException) {
                        // Do nothing
                    }
                }
            }
        }
}

abstract class GeoHashPositionMatch(matcher: Matcher) : PositionMatch(matcher) {
    private var latLonZCache: Triple<Double, Double, Int>? = null
    val latLonZ: Triple<Double, Double, Int>?
        get() = latLonZCache ?: matcher.groupOrNull("hash")?.let { hash -> decode(hash).also { latLonZCache = it } }
    override val points: List<Point>?
        get() = latLonZ?.let { (lat, lon) -> persistentListOf(Point(lat.toString(), lon.toString())) }
    override val z: String?
        get() = latLonZ?.third?.toString()

    abstract fun decode(hash: String): Triple<Double, Double, Int>
}

/**
 * Create a position from a list of matches.
 *
 * Get points from the last match that has not null `points` property. If such a match doesn't exist, find the last
 * match that has not null `lat` property and the last match that has not null `lon` property and create a point from
 * them.
 */
fun List<PositionMatch>.toPosition() = Position(
    points = this.lastNotNullOrNull { it.points?.toImmutableList() }
        ?: this.lastNotNullOrNull { it.lat }?.let { lat ->
            this.lastNotNullOrNull { it.lon }?.let { lon ->
                persistentListOf(Point(lat, lon))
            }
        },
    q = this.lastNotNullOrNull { it.q },
    z = this.lastNotNullOrNull { it.z },
)

open class RedirectMatch(val matcher: Matcher) {
    open val url: String? get() = matcher.groupOrNull("url")
}

fun List<RedirectMatch>.toUrlString() = this.lastNotNullOrNull { it.url }
