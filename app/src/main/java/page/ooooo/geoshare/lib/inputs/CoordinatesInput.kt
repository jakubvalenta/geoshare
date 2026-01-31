package page.ooooo.geoshare.lib.inputs

import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.outputs.CoordinatesOutput
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints
import page.ooooo.geoshare.lib.point.toParseUriResult

object CoordinatesInput : Input {
    @Suppress("SpellCheckingInspection")
    private const val CHARS = """[\p{Zs},°'′"″NSWE]"""
    private const val SPACE = """\p{Zs}*"""
    private const val LAT_SIG = """(-?)"""
    private const val LAT_DEG = """(\d{1,2}(?:\.\d{1,$MAX_PRECISION})?)"""
    private const val LAT_MIN = """(\d{1,2}(?:\.\d{1,$MAX_PRECISION})?)"""
    private const val LAT_SEC = """(\d{1,2}(?:\.\d{1,$MAX_PRECISION})?)"""
    private const val LON_SIG = """(-?)"""
    private const val LON_DEG = """(\d{1,3}(?:\.\d{1,$MAX_PRECISION})?)"""
    private const val LON_MIN = """(\d{1,2}(?:\.\d{1,$MAX_PRECISION})?)"""
    private const val LON_SEC = """(\d{1,2}(?:\.\d{1,$MAX_PRECISION})?)"""

    override val uriPattern = Regex("""[\d.\-\p{Zs},°'′"″NSWE]+""")
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
                Regex("""$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LON_SIG$LON_DEG$CHARS*""").matchEntire(path)?.let { m ->
                    points.add(
                        NaivePoint(
                            degToDec(
                                m.value.contains('S'),
                                m.groupOrNull(1),
                                m.groupOrNull(2),
                            ),
                            degToDec(
                                m.value.contains('W'),
                                m.groupOrNull(3),
                                m.groupOrNull(4),
                            ),
                        )
                    )
                    return@run
                }

                // Degrees minutes seconds, e.g. `41°24'12.2"N 2°10'26.5"E`
                Regex("""$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LAT_MIN$CHARS+$LAT_SEC$CHARS+$SPACE$LON_SIG$LON_DEG$CHARS+$LON_MIN$CHARS+$LON_SEC$CHARS*""").matchEntire(
                    path
                )?.let { m ->
                    points.add(
                        NaivePoint(
                            degToDec(
                                m.value.contains('S'),
                                m.groupOrNull(1),
                                m.groupOrNull(2),
                                m.groupOrNull(3),
                                m.groupOrNull(4),
                            ),
                            degToDec(
                                m.value.contains('W'),
                                m.groupOrNull(5),
                                m.groupOrNull(6),
                                m.groupOrNull(7),
                                m.groupOrNull(8),
                            ),
                        )
                    )
                    return@run
                }

                // Degrees minutes, e.g. `41 24.2028, 2 10.4418`
                Regex("""$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LAT_MIN$CHARS+$LON_SIG$LON_DEG$CHARS+$LON_MIN$CHARS*""").matchEntire(
                    path
                )?.let { m ->
                    points.add(
                        NaivePoint(
                            degToDec(
                                m.value.contains('S'),
                                m.groupOrNull(1),
                                m.groupOrNull(2),
                                m.groupOrNull(3),
                            ),
                            degToDec(
                                m.value.contains('W'),
                                m.groupOrNull(4),
                                m.groupOrNull(5),
                                m.groupOrNull(6),
                            ),
                        )
                    )
                    return@run
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
