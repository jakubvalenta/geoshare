package page.ooooo.geoshare.lib.conversion

import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlinx.io.Source
import kotlinx.io.readLine
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.position.PositionBuilder
import page.ooooo.geoshare.lib.position.Srs
import java.net.URL

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

        fun uriPattern(srs: Srs, block: PositionPattern<Uri>.() -> Unit) = UriPattern(srs, block)

        fun htmlPattern(srs: Srs, block: SourcePattern.() -> Unit) = HtmlPattern(srs, block)
    }

    class PositionPattern<T>(val input: T, val positionBuilder: PositionBuilder) {
        fun setPointFromMatcher(block: T.() -> Matcher?) =
            positionBuilder.setPointFromMatcher { input.block() }

        fun setPointAndZoomFromMatcher(block: T.() -> Matcher?) =
            positionBuilder.setPointAndZoomFromMatcher { input.block() }

        fun setLatLonZoom(block: T.() -> Triple<Double, Double, Double>?) =
            positionBuilder.setLatLonZoom { input.block() }

        fun addPointsFromSequenceOfMatchers(block: T.() -> Sequence<Matcher>) =
            positionBuilder.addPointsFromSequenceOfMatchers { input.block() }

        fun setDefaultPointFromMatcher(block: T.() -> Matcher?) =
            positionBuilder.setDefaultPointFromMatcher { input.block() }

        fun setQueryFromMatcher(block: T.() -> Matcher?) =
            positionBuilder.setQueryFromMatcher { input.block() }

        fun setZoomFromMatcher(block: T.() -> Matcher?) =
            positionBuilder.setZoomFromMatcher { input.block() }

        fun setUrl(block: T.() -> URL?) =
            positionBuilder.setUrl { input.block() }

        fun setUrlFromMatcher(block: T.() -> Matcher?) =
            positionBuilder.setUrlFromMatcher { input.block() }
    }

    class SourcePattern(val input: Source, val positionBuilder: PositionBuilder) {
        fun forEachLine(block: PositionPattern<String>.() -> Unit) {
            for (line in generateSequence { input.readLine() }) {
                PositionPattern(line, positionBuilder).block()
            }
        }
    }

    class UriPattern(val srs: Srs, val block: PositionPattern<Uri>.() -> Unit) {
        fun match(input: Uri): PositionBuilder {
            val positionBuilder = PositionBuilder(srs)
            PositionPattern(input, positionBuilder).block()
            return positionBuilder
        }
    }

    class HtmlPattern(val srs: Srs, val block: SourcePattern.() -> Unit) {
        fun match(input: Source): PositionBuilder {
            val positionBuilder = PositionBuilder(srs)
            SourcePattern(input, positionBuilder).block()
            return positionBuilder
        }
    }
}
