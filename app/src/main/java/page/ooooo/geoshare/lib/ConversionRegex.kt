package page.ooooo.geoshare.lib

import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlin.math.max
import kotlin.math.min

abstract class ConversionRegex(regex: String) {
    protected val pattern: Pattern = Pattern.compile(regex)
    protected var matcher: Matcher? = null

    open fun matches(input: String): Boolean = pattern.matcher(input).also { matcher = it }.matches()

    open fun find(input: String): Boolean = pattern.matcher(input).also { matcher = it }.find()

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
        const val LAT = """[\+ ]?(?P<lat>-?\d{1,2}(\.\d{1,16})?)"""
        const val LON = """[\+ ]?(?P<lon>-?\d{1,3}(\.\d{1,16})?)"""
        const val Z = """(?P<z>\d{1,2}(\.\d{1,16})?)"""
        const val Q_PARAM = """(?P<q>.+)"""
        const val Q_PATH = """(?P<q>[^/]+)"""
    }

    open val lat: String?
        get() = groupOrNull("lat")
    open val lon: String?
        get() = groupOrNull("lon")
    open val q: String?
        get() = groupOrNull("q")
    open val z: String?
        get() = groupOrNull("z")?.toDouble()
            ?.let { max(1.0, min(21.0, it)) }
            ?.toTrimmedString()
    open val points: List<Pair<String, String>>? = null
}

fun List<PositionRegex>.toPosition() = Position(
    this.lastNotNullOrNull { it.lat },
    this.lastNotNullOrNull { it.lon },
    this.lastNotNullOrNull { it.q },
    this.lastNotNullOrNull { it.z },
    this.lastNotNullOrNull { it.points },
)

open class RedirectRegex(regex: String) : ConversionRegex(regex) {
    open val url: String? get() = groupOrNull("url")
}

fun List<RedirectRegex>.toUrlString() = this.lastNotNullOrNull { it.url }
