package page.ooooo.geoshare.lib.conversion

import androidx.compose.runtime.Immutable
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import kotlin.math.max
import kotlin.math.min

class ConversionPattern<I>(val srs: Srs, init: ConversionPattern<I>.() -> Unit) {

    companion object {
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
    }

    sealed interface Block<I> {
        data class LatLon<I>(val block: I.() -> Matcher?) : Block<I>
        data class LatLonAll<I>(val block: I.() -> Sequence<Matcher>) : Block<I>
        data class LatLonZ<I>(val block: I.() -> Matcher?) : Block<I>
        data class Q<I>(val block: I.() -> Matcher?) : Block<I>
        data class Z<I>(val block: I.() -> Matcher?) : Block<I>
        data class HtmlUri<I>(val block: I.() -> Uri?) : Block<I>
    }

    @Immutable
    data class Result(val position: Position, val htmlUri: Uri? = null)

    val blocks: MutableList<Block<I>> = mutableListOf()

    init {
        this.init()
    }

    fun latLon(block: I.() -> Matcher?) {
        blocks.add(Block.LatLon(block))
    }

    fun latLonAll(block: I.() -> Sequence<Matcher>) {
        blocks.add(Block.LatLonAll(block))
    }

    fun latLonZ(block: I.() -> Matcher?) {
        blocks.add(Block.LatLonZ(block))
    }

    fun q(block: I.() -> Matcher?) {
        blocks.add(Block.Q(block))
    }

    fun z(block: I.() -> Matcher?) {
        blocks.add(Block.Z(block))
    }

    fun htmlUri(block: I.() -> Uri?) {
        blocks.add(Block.HtmlUri(block))
    }

    fun match(input: I): Result {
        var points: ImmutableList<Point>? = null
        var q: String? = null
        var z: Double? = null
        var htmlUri: Uri? = null

        for (matcher in blocks) {
            when (matcher) {
                is Block.LatLon<I> ->
                    if (points == null) {
                        matcher.block.invoke(input)?.let { m ->
                            m.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
                                m.groupOrNull("lon")?.toDoubleOrNull()?.let { lon ->
                                    points = persistentListOf(Point(srs, lat, lon))
                                }
                            }
                        }
                    }

                is Block.LatLonAll<I> ->
                    if (points == null) {
                        points = matcher.block.invoke(input)
                            .mapNotNull { m ->
                                m.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
                                    m.groupOrNull("lon")?.toDoubleOrNull()?.let { lon ->
                                        Point(srs, lat, lon)
                                    }
                                }
                            }
                            .toImmutableList()
                            .takeIf { it.isNotEmpty() }
                    }

                is Block.LatLonZ<I> ->
                    if (points == null) {
                        matcher.block.invoke(input)?.let { m ->
                            m.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
                                m.groupOrNull("lon")?.toDoubleOrNull()?.let { lon ->
                                    m.groupOrNull("z")?.toDoubleOrNull()?.let { newZ ->
                                        points = persistentListOf(Point(srs, lat, lon))
                                        z = max(1.0, min(21.0, newZ))
                                    }
                                }
                            }
                        }
                    }

                is Block.Q<I> -> {
                    if (q == null) {
                        matcher.block.invoke(input)
                            ?.groupOrNull("q")
                            ?.let { q = it }
                    }
                }

                is Block.Z<I> -> {
                    if (z == null) {
                        matcher.block.invoke(input)
                            ?.groupOrNull("z")
                            ?.toDoubleOrNull()
                            ?.let { z = max(1.0, min(21.0, it)) }
                    }
                }

                is Block.HtmlUri<I> -> {
                    if (htmlUri == null) {
                        matcher.block.invoke(input)
                            ?.let { htmlUri = it }
                    }
                }
            }
        }

        return Result(
            position = Position(points, q = q, z = z),
            htmlUri = htmlUri,
        )
    }
}
