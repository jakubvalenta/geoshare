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
    open val lat: Double?
        get() = matcher.groupOrNull("lat")?.toDoubleOrNull()
    open val lon: Double?
        get() = matcher.groupOrNull("lon")?.toDoubleOrNull()
    open val q: String?
        get() = matcher.groupOrNull("q")
    open val z: Double?
        get() = matcher.groupOrNull("z")?.toDoubleOrNull()?.let { max(1.0, min(21.0, it)) }
}

/**
 * Repeatedly searches for LAT and LON in the input to get points.
 */
class PointsPositionMatch(matcher: Matcher) : PositionMatch(matcher) {
    override val points
        get() = matcher.reset().let { m ->
            buildList {
                while (m.find()) {
                    val lat = m.groupOrNull("lat")?.toDoubleOrNull() ?: continue
                    val lon = m.groupOrNull("lon")?.toDoubleOrNull() ?: continue
                    add(Point(lat, lon))
                }
            }
        }
}

abstract class GeoHashPositionMatch(matcher: Matcher) : PositionMatch(matcher) {
    private var latLonZCache: Triple<Double, Double, Double>? = null
    val latLonZ: Triple<Double, Double, Double>?
        get() = latLonZCache ?: matcher.groupOrNull("hash")?.let { hash -> decode(hash).also { latLonZCache = it } }
    override val points get() = latLonZ?.let { (lat, lon) -> persistentListOf(Point(lat, lon)) }
    override val z get() = latLonZ?.third

    abstract fun decode(hash: String): Triple<Double, Double, Double>
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
