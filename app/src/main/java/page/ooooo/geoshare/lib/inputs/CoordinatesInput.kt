package page.ooooo.geoshare.lib.inputs

import androidx.compose.ui.res.stringResource
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ConversionPattern
import page.ooooo.geoshare.lib.ConversionPattern.Companion.MAX_COORD_PRECISION
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matches
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.outputs.CoordinatesOutputGroup
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

object CoordinatesInput : Input.HasUri {
    @Suppress("SpellCheckingInspection")
    private const val CHARS = """[\p{Zs},°'′"″NSWE]"""
    private const val SPACE = """\p{Zs}*"""
    private const val LAT_SIG = """(?P<latSig>-?)"""
    private const val LAT_DEG = """(?P<latDeg>\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?)"""
    private const val LAT_MIN = """(?P<latMin>\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?)"""
    private const val LAT_SEC = """(?P<latSec>\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?)"""
    private const val LON_SIG = """(?P<lonSig>-?)"""
    private const val LON_DEG = """(?P<lonDeg>\d{1,3}(\.\d{1,$MAX_COORD_PRECISION})?)"""
    private const val LON_MIN = """(?P<lonMin>\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?)"""
    private const val LON_SEC = """(?P<lonSec>\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?)"""

    private val srs = Srs.WGS84

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""[\d\.\-\p{Zs},°'′"″NSWE]+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_coordinates_name,
        inputs = listOf(
            Input.DocumentationInput.Text(20) {
                stringResource(R.string.example, CoordinatesOutputGroup.formatDegMinSecString(Position.example))
            },
        ),
    )

    override val conversionUriPattern = ConversionPattern.first<Uri, Position> {
        // Decimal, e.g. `N 41.40338, E 2.17403`
        pattern {
            (path matches """$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LON_SIG$LON_DEG$CHARS*""")?.let { m ->
                Position(
                    srs,
                    lat = degToDec(
                        m.groupOrNull()?.contains('S') == true,
                        m.groupOrNull("latSig"),
                        m.groupOrNull("latDeg"),
                    ),
                    lon = degToDec(
                        m.groupOrNull()?.contains('W') == true,
                        m.groupOrNull("lonSig"),
                        m.groupOrNull("lonDeg"),
                    ),
                )
            }
        }

        // Degrees minutes seconds, e.g. `41°24'12.2"N 2°10'26.5"E`
        pattern {
            (path matches """$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LAT_MIN$CHARS+$LAT_SEC$CHARS+$SPACE$LON_SIG$LON_DEG$CHARS+$LON_MIN$CHARS+$LON_SEC$CHARS*""")?.let { m ->
                Position(
                    srs,
                    degToDec(
                        m.groupOrNull()?.contains('S') == true,
                        m.groupOrNull("latSig"),
                        m.groupOrNull("latDeg"),
                        m.groupOrNull("latMin"),
                        m.groupOrNull("latSec"),
                    ),
                    degToDec(
                        m.groupOrNull()?.contains('W') == true,
                        m.groupOrNull("lonSig"),
                        m.groupOrNull("lonDeg"),
                        m.groupOrNull("lonMin"),
                        m.groupOrNull("lonSec"),
                    ),
                )
            }
        }

        // Degrees minutes, e.g. `41 24.2028, 2 10.4418`
        pattern {
            (path matches """$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LAT_MIN$CHARS+$LON_SIG$LON_DEG$CHARS+$LON_MIN$CHARS*""")?.let { m ->
                Position(
                    srs,
                    degToDec(
                        m.groupOrNull()?.contains('S') == true,
                        m.groupOrNull("latSig"),
                        m.groupOrNull("latDeg"),
                        m.groupOrNull("latMin"),
                    ),
                    degToDec(
                        m.groupOrNull()?.contains('W') == true,
                        m.groupOrNull("lonSig"),
                        m.groupOrNull("lonDeg"),
                        m.groupOrNull("lonMin"),
                    ),
                )
            }
        }
    }

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
