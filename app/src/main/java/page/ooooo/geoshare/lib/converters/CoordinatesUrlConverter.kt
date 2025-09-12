package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.allUriPattern
import java.math.RoundingMode

class CoordinatesUrlConverter : UrlConverter.WithUriPattern {
    override val uriPattern: Pattern = Pattern.compile("""[\p{Zs}\d\.,°'′"″NSWE-]+""")

    override val conversionUriPattern = allUriPattern {
        first {
            val comma = """[,p{Zs}]+"""
            val space = """\p{Zs}*"""
            val degree = """°"""
            val prime = """['′\p{Zs}]"""
            val doublePrime = """["″\p{Zs}]"""
            val northSouthBefore = """(?P<northSouthBefore>[NS]?)"""
            val northSouthAfter = """(?P<northSouthAfter>[NS]?)"""
            val latSig = """(?P<latSig>-?)"""
            val latDeg = """(?P<latDeg>\d{1,2}(\.\d{1,16})?)"""
            val latMin = """(?P<latMin>\d{1,2}(\.\d{1,16})?)"""
            val latSec = """(?P<latSec>\d{1,2}(\.\d{1,16})?)"""
            val westEastBefore = """(?P<westEastBefore>[WE]?)"""
            val westEastAfter = """(?P<westEastAfter>[WE]?)"""
            val lonSig = """(?P<lonSig>-?)"""
            val lonDeg = """(?P<lonDeg>\d{1,3}(\.\d{1,16})?)"""
            val lonMin = """(?P<lonMin>\d{1,2}(\.\d{1,16})?)"""
            val lonSec = """(?P<lonSec>\d{1,2}(\.\d{1,16})?)"""

            // Decimal, e.g. `N 41.40338, E 2.17403`
            path("""$space$northSouthBefore$space$latSig$latDeg$degree?$northSouthAfter$comma$westEastBefore$space$lonSig$lonDeg$degree?$westEastAfter$space""") { name, value ->
                when (name) {
                    "lat" -> degToDec(
                        groupOrNull(matcher, "latSig"),
                        groupOrNull(matcher, "latDeg"),
                        northSouthWestEast = groupOrNull(matcher, "northSouthBefore")?.takeIf { it.isNotEmpty() }
                            ?: groupOrNull(matcher, "northSouthAfter"),
                    )

                    "lon" -> degToDec(
                        groupOrNull(matcher, "lonSig"),
                        groupOrNull(matcher, "lonDeg"),
                        northSouthWestEast = groupOrNull(matcher, "westEastBefore")?.takeIf { it.isNotEmpty() }
                            ?: groupOrNull(matcher, "westEastAfter"),
                    )

                    else -> value
                }
            }

            // Degrees minutes seconds, e.g. `41°24'12.2"N 2°10'26.5"E`
            path("""$space$northSouthBefore$space$latSig$latDeg$degree$latMin$prime$latSec$doublePrime$northSouthAfter$comma$westEastBefore$space$lonSig$lonDeg$degree$lonMin$prime$lonSec$doublePrime$westEastAfter$space""") { name, value ->
                when (name) {
                    "lat" -> degToDec(
                        groupOrNull(matcher, "latSig"),
                        groupOrNull(matcher, "latDeg"),
                        groupOrNull(matcher, "latMin"),
                        groupOrNull(matcher, "latSec"),
                        groupOrNull(matcher, "northSouthBefore")?.takeIf { it.isNotEmpty() }
                            ?: groupOrNull(matcher, "northSouthAfter"),
                    )

                    "lon" -> degToDec(
                        groupOrNull(matcher, "lonSig"),
                        groupOrNull(matcher, "lonDeg"),
                        groupOrNull(matcher, "lonMin"),
                        groupOrNull(matcher, "lonSec"),
                        groupOrNull(matcher, "westEastBefore")?.takeIf { it.isNotEmpty() }
                            ?: groupOrNull(matcher, "westEastAfter"),
                    )

                    else -> value
                }
            }

            // Degrees minutes, e.g. `41 24.2028, 2 10.4418`
            path(
                """$space$latSig$latDeg$latMin$comma$lonSig$lonDeg$lonMin$space"""
            ) { name, value ->
                when (name) {
                    "lat" -> degToDec(
                        groupOrNull(matcher, "latSig"),
                        groupOrNull(matcher, "latDeg"),
                        groupOrNull(matcher, "latMin"),
                    )

                    "lon" -> degToDec(
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
        sig: String?,
        deg: String?,
        minutes: String? = null,
        seconds: String? = null,
        northSouthWestEast: String? = null,
    ): String {
        val sig = if (sig == "-") -1 else 1
        val deg = (deg?.toDouble() ?: 0.0)
        val minutes = (minutes?.toDouble() ?: 0.0) / 60
        val seconds = (seconds?.toDouble() ?: 0.0) / 3600
        val northSouthWestEast = if (northSouthWestEast == "S" || northSouthWestEast == "W") -1 else 1
        val dec = (sig * northSouthWestEast * (deg + minutes + seconds))
        return dec.toBigDecimal().setScale(6, RoundingMode.HALF_UP).toDouble().toString()
    }
}
