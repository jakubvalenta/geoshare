package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.PositionRegex
import page.ooooo.geoshare.lib.toScale
import page.ooooo.geoshare.lib.uriPattern
import page.ooooo.geoshare.R

class CoordinatesUrlConverter : UrlConverter.WithUriPattern {
    companion object {
        @Suppress("SpellCheckingInspection")
        const val CHARS = """[\p{Zs},°'′"″NSWE]"""
        const val SPACE = """\p{Zs}*"""
        const val LAT_SIG = """(?P<latSig>-?)"""
        const val LAT_DEG = """(?P<latDeg>\d{1,2}(\.\d{1,16})?)"""
        const val LAT_MIN = """(?P<latMin>\d{1,2}(\.\d{1,16})?)"""
        const val LAT_SEC = """(?P<latSec>\d{1,2}(\.\d{1,16})?)"""
        const val LON_SIG = """(?P<lonSig>-?)"""
        const val LON_DEG = """(?P<lonDeg>\d{1,3}(\.\d{1,16})?)"""
        const val LON_MIN = """(?P<lonMin>\d{1,2}(\.\d{1,16})?)"""
        const val LON_SEC = """(?P<lonSec>\d{1,2}(\.\d{1,16})?)"""
    }

    @StringRes
    override val nameResId = R.string.converter_coordinates_name

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""[\d\.\-\p{Zs},°'′"″NSWE]+""")
    override val supportedUriStrings = emptyList<String>()

    override val conversionUriPattern = uriPattern {
        // Decimal, e.g. `N 41.40338, E 2.17403`
        path(object : PositionRegex("""$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LON_SIG$LON_DEG$CHARS*""") {
            override val points: List<Point>
                get() {
                    val lat = degToDec(
                        groupOrNull()?.contains('S') == true,
                        groupOrNull("latSig"),
                        groupOrNull("latDeg"),
                    )
                    val lon = degToDec(
                        groupOrNull()?.contains('W') == true,
                        groupOrNull("lonSig"),
                        groupOrNull("lonDeg"),
                    )
                    return listOf(lat to lon)
                }
        })

        // Degrees minutes seconds, e.g. `41°24'12.2"N 2°10'26.5"E`
        path(object :
            PositionRegex("""$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LAT_MIN$CHARS+$LAT_SEC$CHARS+$SPACE$LON_SIG$LON_DEG$CHARS+$LON_MIN$CHARS+$LON_SEC$CHARS*""") {
            override val points: List<Point>
                get() {
                    val lat = degToDec(
                        groupOrNull()?.contains('S') == true,
                        groupOrNull("latSig"),
                        groupOrNull("latDeg"),
                        groupOrNull("latMin"),
                        groupOrNull("latSec"),
                    )
                    val lon = degToDec(
                        groupOrNull()?.contains('W') == true,
                        groupOrNull("lonSig"),
                        groupOrNull("lonDeg"),
                        groupOrNull("lonMin"),
                        groupOrNull("lonSec"),
                    )
                    return listOf(lat to lon)
                }
        })

        // Degrees minutes, e.g. `41 24.2028, 2 10.4418`
        path(object :
            PositionRegex("""$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LAT_MIN$CHARS+$LON_SIG$LON_DEG$CHARS+$LON_MIN$CHARS*""") {
            override val points: List<Point>
                get() {
                    val lat = degToDec(
                        groupOrNull()?.contains('S') == true,
                        groupOrNull("latSig"),
                        groupOrNull("latDeg"),
                        groupOrNull("latMin"),
                    )
                    val lon = degToDec(
                        groupOrNull()?.contains('W') == true,
                        groupOrNull("lonSig"),
                        groupOrNull("lonDeg"),
                        groupOrNull("lonMin"),
                    )
                    return listOf(lat to lon)
                }
        })
    }

    private fun degToDec(
        southOrWest: Boolean,
        sig: String?,
        deg: String?,
        min: String? = null,
        sec: String? = null,
    ): String {
        val sig = if (southOrWest && sig != "-" || !southOrWest && sig == "-") -1 else 1
        val deg = (deg?.toDouble() ?: 0.0)
        val min = (min?.toDouble() ?: 0.0) / 60
        val sec = (sec?.toDouble() ?: 0.0) / 3600
        val dec = (sig * (deg + min + sec))
        return dec.toScale(6).toString()
    }
}
