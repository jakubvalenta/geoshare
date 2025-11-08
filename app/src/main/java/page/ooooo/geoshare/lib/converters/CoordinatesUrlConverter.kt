package page.ooooo.geoshare.lib.converters

import androidx.compose.ui.res.stringResource
import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionMatch.Companion.MAX_COORD_PRECISION
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matches
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.outputs.CoordinatesOutputGroup

class CoordinatesUrlConverter : UrlConverter.WithUriPattern {
    companion object {
        @Suppress("SpellCheckingInspection")
        const val CHARS = """[\p{Zs},°'′"″NSWE]"""
        const val SPACE = """\p{Zs}*"""
        const val LAT_SIG = """(?P<latSig>-?)"""
        const val LAT_DEG = """(?P<latDeg>\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?)"""
        const val LAT_MIN = """(?P<latMin>\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?)"""
        const val LAT_SEC = """(?P<latSec>\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?)"""
        const val LON_SIG = """(?P<lonSig>-?)"""
        const val LON_DEG = """(?P<lonDeg>\d{1,3}(\.\d{1,$MAX_COORD_PRECISION})?)"""
        const val LON_MIN = """(?P<lonMin>\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?)"""
        const val LON_SEC = """(?P<lonSec>\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?)"""

        private fun degToDec(
            southOrWest: Boolean,
            sig: String?,
            deg: String?,
            min: String? = null,
            sec: String? = null,
        ): Double {
            val sig = if (southOrWest && sig != "-" || !southOrWest && sig == "-") -1 else 1
            val deg = (deg?.toDouble() ?: 0.0)
            val min = (min?.toDouble() ?: 0.0) / 60
            val sec = (sec?.toDouble() ?: 0.0) / 3600
            val dec = (sig * (deg + min + sec))
            return dec.toScale(6)
        }
    }

    private val srs = Srs.WGS84

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""[\d\.\-\p{Zs},°'′"″NSWE]+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_coordinates_name,
        inputs = listOf(
            DocumentationInput.Text(20) {
                stringResource(R.string.example, CoordinatesOutputGroup.formatDegMinSecString(Position.example))
            },
        ),
    )

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        on { path matches """$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LON_SIG$LON_DEG$CHARS*""" } doReturn
                { DecimalCoordsPositionMatch(it, srs) }
        on { path matches """$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LAT_MIN$CHARS+$LAT_SEC$CHARS+$SPACE$LON_SIG$LON_DEG$CHARS+$LON_MIN$CHARS+$LON_SEC$CHARS*""" } doReturn
                { DegreesMinutesSecondsCoordsPositionMatch(it, srs) }
        on { path matches """$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LAT_MIN$CHARS+$LON_SIG$LON_DEG$CHARS+$LON_MIN$CHARS*""" } doReturn
                { DegreesMinutesCoordsPositionMatch(it, srs) }
    }

    /**
     * Decimal, e.g. `N 41.40338, E 2.17403`
     */
    private class DecimalCoordsPositionMatch(matcher: Matcher, srs: Srs) : PositionMatch(matcher, srs) {
        override val points: List<Point>
            get() {
                val lat = degToDec(
                    matcher.groupOrNull()?.contains('S') == true,
                    matcher.groupOrNull("latSig"),
                    matcher.groupOrNull("latDeg"),
                )
                val lon = degToDec(
                    matcher.groupOrNull()?.contains('W') == true,
                    matcher.groupOrNull("lonSig"),
                    matcher.groupOrNull("lonDeg"),
                )
                return persistentListOf(Point(srs, lat, lon))
            }
    }

    /**
     * Degrees minutes seconds, e.g. `41°24'12.2"N 2°10'26.5"E`
     */
    private class DegreesMinutesSecondsCoordsPositionMatch(matcher: Matcher, srs: Srs) : PositionMatch(matcher, srs) {
        override val points: List<Point>
            get() {
                val lat = degToDec(
                    matcher.groupOrNull()?.contains('S') == true,
                    matcher.groupOrNull("latSig"),
                    matcher.groupOrNull("latDeg"),
                    matcher.groupOrNull("latMin"),
                    matcher.groupOrNull("latSec"),
                )
                val lon = degToDec(
                    matcher.groupOrNull()?.contains('W') == true,
                    matcher.groupOrNull("lonSig"),
                    matcher.groupOrNull("lonDeg"),
                    matcher.groupOrNull("lonMin"),
                    matcher.groupOrNull("lonSec"),
                )
                return persistentListOf(Point(srs, lat, lon))
            }
    }

    /**
     * Degrees minutes, e.g. `41 24.2028, 2 10.4418`
     */
    private class DegreesMinutesCoordsPositionMatch(matcher: Matcher, srs: Srs) : PositionMatch(matcher, srs) {
        override val points: List<Point>
            get() {
                val lat = degToDec(
                    matcher.groupOrNull()?.contains('S') == true,
                    matcher.groupOrNull("latSig"),
                    matcher.groupOrNull("latDeg"),
                    matcher.groupOrNull("latMin"),
                )
                val lon = degToDec(
                    matcher.groupOrNull()?.contains('W') == true,
                    matcher.groupOrNull("lonSig"),
                    matcher.groupOrNull("lonDeg"),
                    matcher.groupOrNull("lonMin"),
                )
                return persistentListOf(Point(srs, lat, lon))
            }
    }
}
