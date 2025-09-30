package page.ooooo.geoshare.lib

import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlin.math.max
import kotlin.math.min

abstract class ConversionRegex(regex: String) {
    protected val pattern: Pattern = Pattern.compile(regex)
    protected var matcher: Matcher? = null
    protected var input: String? = null

    open fun matches(input: String): Boolean {
        this.input = input
        matcher = pattern.matcher(input)
        return matcher?.matches() == true
    }

    open fun find(input: String): Boolean {
        this.input = input
        matcher = pattern.matcher(input)
        return matcher?.find() == true
    }

    protected fun groupOrNull(): String? = try {
        matcher?.group()
    } catch (_: IllegalArgumentException) {
        null
    }

    protected fun groupOrNull(name: String): String? = try {
        matcher?.group(name)
    } catch (_: IllegalArgumentException) {
        null
    }
}

open class PositionRegex(regex: String) : ConversionRegex(regex) {
    companion object {
        const val LAT_NUM = """-?\d{1,2}(\.\d{1,16})?"""
        const val LON_NUM = """-?\d{1,3}(\.\d{1,16})?"""
        const val LAT = """[\+ ]?(?P<lat>$LAT_NUM)"""
        const val LON = """[\+ ]?(?P<lon>$LON_NUM)"""
        const val Z = """(?P<z>\d{1,2}(\.\d{1,16})?)"""
        const val Q_PARAM = """(?P<q>.+)"""
        const val Q_PATH = """(?P<q>[^/]+)"""
    }

    open val points: List<Point>?
        get() = lat?.let { lat -> lon?.let { lon -> listOf(lat to lon) } }
    open val lat: String?
        get() = groupOrNull("lat")
    open val lon: String?
        get() = groupOrNull("lon")
    open val q: String?
        get() = groupOrNull("q")
    open val z: String?
        get() = groupOrNull("z")?.toDouble()?.let { max(1.0, min(21.0, it)) }?.toTrimmedString()
}

/**
 * Repeatedly searches for LAT and LON in the input to get points.
 */
class PointsPositionRegex(regex: String) : PositionRegex(regex) {
    override val points: List<Point>
        get() = pattern.matcher(input).let { m ->
            mutableListOf<Point>().apply {
                while (m.find()) {
                    try {
                        add(m.group("lat") to m.group("lon"))
                    } catch (_: IllegalArgumentException) {
                        // Do nothing
                    }
                }
            }
        }
}

/**
 * Create a position from a list of regexes.
 *
 * Get points from the last regex that has not null `points` property. If such a regex doesn't exist, find the last
 * regex that has not null `lat` property and the last regex that has not null `lon` property and create a point from
 * them.
 */
fun List<PositionRegex>.toPosition() = Position(
    this.lastNotNullOrNull { it.points }
        ?: this.lastNotNullOrNull { it.lat }?.let { lat ->
            this.lastNotNullOrNull { it.lon }?.let { lon ->
                listOf(lat to lon)
            }
        },
    this.lastNotNullOrNull { it.q },
    this.lastNotNullOrNull { it.z },
)

open class RedirectRegex(regex: String) : ConversionRegex(regex) {
    open val url: String? get() = groupOrNull("url")
}

fun List<RedirectRegex>.toUrlString() = this.lastNotNullOrNull { it.url }
