package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.allUriPattern
import java.math.RoundingMode

class CoordinatesUrlConverter : UrlConverter.WithUriPattern {
    override val uriPattern: Pattern = Pattern.compile("""[\d\.\-\p{Zs},°'′"″NSWE]+""")

    override val conversionUriPattern = allUriPattern {
        first {
            val chars = """[\p{Zs},°'′"″NSWE]"""
            val space = """\p{Zs}*"""
            val latSig = """(?P<latSig>-?)"""
            val latDeg = """(?P<latDeg>\d{1,2}(\.\d{1,16})?)"""
            val latMin = """(?P<latMin>\d{1,2}(\.\d{1,16})?)"""
            val latSec = """(?P<latSec>\d{1,2}(\.\d{1,16})?)"""
            val lonSig = """(?P<lonSig>-?)"""
            val lonDeg = """(?P<lonDeg>\d{1,3}(\.\d{1,16})?)"""
            val lonMin = """(?P<lonMin>\d{1,2}(\.\d{1,16})?)"""
            val lonSec = """(?P<lonSec>\d{1,2}(\.\d{1,16})?)"""

            // Decimal, e.g. `N 41.40338, E 2.17403`
            path("""$chars*$latSig$latDeg$chars+$lonSig$lonDeg$chars*""") { name, value ->
                when (name) {
                    "lat" -> degToDec(
                        "S" in matcher.group(),
                        groupOrNull(matcher, "latSig"),
                        groupOrNull(matcher, "latDeg"),
                    )

                    "lon" -> degToDec(
                        "W" in matcher.group(),
                        groupOrNull(matcher, "lonSig"),
                        groupOrNull(matcher, "lonDeg"),
                    )

                    else -> value
                }
            }

            // Degrees minutes seconds, e.g. `41°24'12.2"N 2°10'26.5"E`
            path("""$chars*$latSig$latDeg$chars+$latMin$chars+$latSec$chars+$space$lonSig$lonDeg$chars+$lonMin$chars+$lonSec$chars*""") { name, value ->
                when (name) {
                    "lat" -> degToDec(
                        "S" in matcher.group(),
                        groupOrNull(matcher, "latSig"),
                        groupOrNull(matcher, "latDeg"),
                        groupOrNull(matcher, "latMin"),
                        groupOrNull(matcher, "latSec"),
                    )

                    "lon" -> degToDec(
                        "W" in matcher.group(),
                        groupOrNull(matcher, "lonSig"),
                        groupOrNull(matcher, "lonDeg"),
                        groupOrNull(matcher, "lonMin"),
                        groupOrNull(matcher, "lonSec"),
                    )

                    else -> value
                }
            }

            // Degrees minutes, e.g. `41 24.2028, 2 10.4418`
            path("""$chars*$latSig$latDeg$chars+$latMin$chars+$lonSig$lonDeg$chars+$lonMin$chars*""") { name, value ->
                when (name) {
                    "lat" -> degToDec(
                        "S" in matcher.group(),
                        groupOrNull(matcher, "latSig"),
                        groupOrNull(matcher, "latDeg"),
                        groupOrNull(matcher, "latMin"),
                    )

                    "lon" -> degToDec(
                        "W" in matcher.group(),
                        groupOrNull(matcher, "lonSig"),
                        groupOrNull(matcher, "lonDeg"),
                        groupOrNull(matcher, "lonMin"),
                    )

                    else -> value
                }
            }
        }
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
        return dec.toBigDecimal().setScale(6, RoundingMode.HALF_UP).toDouble().toString()
    }
}
