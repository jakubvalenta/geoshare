package page.ooooo.geoshare.lib.inputs

import androidx.compose.ui.res.stringResource
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.outputs.CoordinatesOutput
import page.ooooo.geoshare.lib.point.*

object CoordinatesInput : Input {
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

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""[\d\.\-\p{Zs},°'′"″NSWE]+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.COORDINATES,
        nameResId = R.string.converter_coordinates_name,
        items = listOf(
            InputDocumentationItem.Text(20) {
                stringResource(R.string.example, CoordinatesOutput.formatDegMinSecString(Point.example) ?: "0 E, 0 N")
            },
        ),
    )

    override suspend fun parseUri(uri: Uri): ParseUriResult? =
        buildPoints {
            uri.run {
                // Decimal, e.g. `N 41.40338, E 2.17403`
                setPointIfNull {
                    ("""$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LON_SIG$LON_DEG$CHARS*""" match path)?.let { m ->
                        NaivePoint(
                            degToDec(
                                m.groupOrNull()?.contains('S') == true,
                                m.groupOrNull("latSig"),
                                m.groupOrNull("latDeg"),
                            ),
                            degToDec(
                                m.groupOrNull()?.contains('W') == true,
                                m.groupOrNull("lonSig"),
                                m.groupOrNull("lonDeg"),
                            ),
                        )
                    }
                }

                // Degrees minutes seconds, e.g. `41°24'12.2"N 2°10'26.5"E`
                setPointIfNull {
                    ("""$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LAT_MIN$CHARS+$LAT_SEC$CHARS+$SPACE$LON_SIG$LON_DEG$CHARS+$LON_MIN$CHARS+$LON_SEC$CHARS*""" match path)?.let { m ->
                        NaivePoint(
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
                setPointIfNull {
                    ("""$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LAT_MIN$CHARS+$LON_SIG$LON_DEG$CHARS+$LON_MIN$CHARS*""" match path)?.let { m ->
                        NaivePoint(
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
        }
            .asWGS84()
            .toParseUriResult()

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
