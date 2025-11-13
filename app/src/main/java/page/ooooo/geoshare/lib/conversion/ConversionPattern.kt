package page.ooooo.geoshare.lib.conversion

import androidx.compose.runtime.Immutable
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.io.Source
import kotlinx.io.readLine
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.position.*

interface ConversionPattern {

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

        fun uriPattern(srs: Srs, init: ConversionUriPattern.() -> Unit): ConversionUriPattern {
            val conversionUriPattern = ConversionUriPattern(srs)
            conversionUriPattern.init()
            return conversionUriPattern
        }

        fun htmlPattern(srs: Srs, init: ConversionHtmlPattern.() -> Unit): ConversionHtmlPattern {
            val conversionHtmlPattern = ConversionHtmlPattern(srs)
            conversionHtmlPattern.init()
            return conversionHtmlPattern
        }
    }

    @Immutable
    data class Result(val position: Position, val uriString: String? = null)

    abstract class Group<C> {
        protected val children: MutableList<C> = mutableListOf()

        protected fun <P : C> initChild(child: P, init: P.() -> Unit = {}): P {
            child.init()
            children.add(child)
            return child
        }
    }

    sealed interface UriPattern

    class PointsUriPattern(private val block: Uri.() -> Matcher?) : UriPattern {
        fun match(srs: Srs, uri: Uri): ImmutableList<Point>? =
            block.invoke(uri)?.toPoint(srs)?.let { persistentListOf(it) }
    }

    class PointsAndZoomUriPattern(private val block: Uri.() -> Matcher?) : UriPattern {
        fun match(srs: Srs, uri: Uri): Pair<ImmutableList<Point>, Double>? =
            block.invoke(uri)?.toPointAndZ(srs)?.let { (point, z) -> persistentListOf(point) to z }
    }

    class PointsSequenceUriPattern(private val block: Uri.() -> Sequence<Matcher>) : UriPattern {
        fun match(srs: Srs, uri: Uri): ImmutableList<Point>? =
            block.invoke(uri).mapNotNull { it.toPoint(srs) }.toImmutableList().takeIf { it.isNotEmpty() }
    }

    class QueryUriPattern(private val block: Uri.() -> Matcher?) : UriPattern {
        fun match(uri: Uri): String? = block.invoke(uri)?.toQ()
    }

    class ZoomUriPattern(private val block: Uri.() -> Matcher?) : UriPattern {
        fun match(uri: Uri): Double? = block.invoke(uri)?.toZ()
    }

    class UriStringUriPattern(private val block: Uri.() -> Uri?) : UriPattern {
        fun match(uri: Uri): String? = block.invoke(uri)?.toString()
    }

    class ConversionUriPattern(private val srs: Srs) : Group<UriPattern>() {

        fun points(block: Uri.() -> Matcher?) = initChild(PointsUriPattern(block))

        fun pointsAndZoom(block: Uri.() -> Matcher?) = initChild(PointsAndZoomUriPattern(block))

        fun pointsSequence(block: Uri.() -> Sequence<Matcher>) = initChild(PointsSequenceUriPattern(block))

        fun query(block: Uri.() -> Matcher?) = initChild(QueryUriPattern(block))

        fun zoom(block: Uri.() -> Matcher?) = initChild(ZoomUriPattern(block))

        fun uriString(block: Uri.() -> Uri?) = initChild(UriStringUriPattern(block))

        fun match(uri: Uri): Result {
            var points: ImmutableList<Point>? = null
            var q: String? = null
            var z: Double? = null
            var uriString: String? = null

            for (child in children) {
                when (child) {
                    is PointsUriPattern -> if (points == null) {
                        child.match(srs, uri)?.let { points = it }
                    }

                    is PointsAndZoomUriPattern -> if (points == null) {
                        child.match(srs, uri)?.let {
                            points = it.first
                            z = it.second
                        }
                    }

                    is PointsSequenceUriPattern -> if (points == null) {
                        points = child.match(srs, uri)
                    }

                    is QueryUriPattern -> if (q == null) {
                        q = child.match(uri)
                    }

                    is ZoomUriPattern -> if (z == null) {
                        z = child.match(uri)
                    }

                    is UriStringUriPattern -> if (uriString == null) {
                        uriString = child.match(uri)
                    }
                }
            }

            return Result(
                position = Position(points, q = q, z = z),
                uriString = uriString,
            )
        }
    }

    sealed interface LinePattern

    data class PointLinePattern(private val block: String.() -> Matcher?) : LinePattern {
        fun match(srs: Srs, line: String): Point? = block.invoke(line)?.toPoint(srs)
    }

    data class DefaultPointsLinePattern(private val block: String.() -> Matcher?) : LinePattern {
        fun match(srs: Srs, line: String): ImmutableList<Point>? =
            block.invoke(line)?.toPoint(srs)?.let { persistentListOf(it) }
    }

    data class UriStringLinePattern(private val block: String.() -> Matcher?) : LinePattern {
        fun match(line: String): String? = block.invoke(line)?.toUriString()
    }

    sealed interface HtmlPattern

    class ForEachLinePattern : Group<LinePattern>(), HtmlPattern {

        fun point(block: String.() -> Matcher?) = initChild(PointLinePattern(block))

        fun defaultPoints(block: String.() -> Matcher?) = initChild(DefaultPointsLinePattern(block))

        fun uriString(block: String.() -> Matcher?) = initChild(UriStringLinePattern(block))

        fun match(srs: Srs, input: Source): Result {
            val points: MutableList<Point> = mutableListOf()
            var defaultPoints: ImmutableList<Point>? = null
            var uriString: String? = null

            for (line in generateSequence { input.readLine() }) {
                for (child in children) {
                    when (child) {
                        is PointLinePattern ->
                            child.match(srs, line)?.let { points.add(it) }

                        is DefaultPointsLinePattern -> if (defaultPoints == null) {
                            child.match(srs, line)?.let { defaultPoints = it }
                        }

                        is UriStringLinePattern -> if (uriString == null) {
                            child.match(line).let { uriString = it }
                        }
                    }
                }
            }

            return Result(
                position = Position(points.takeIf { it.isNotEmpty() }?.toImmutableList() ?: defaultPoints),
                uriString = uriString,
            )
        }
    }

    class ConversionHtmlPattern(private val srs: Srs) : Group<HtmlPattern>() {

        fun forEachLine(init: ForEachLinePattern.() -> Unit) = initChild(ForEachLinePattern(), init)

        fun match(input: Source): Result? {
            for (child in children) {
                when (child) {
                    is ForEachLinePattern -> return child.match(srs, input)
                }
            }
            return null
        }
    }
}
