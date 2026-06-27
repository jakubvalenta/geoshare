package page.ooooo.geoshare.lib.inputs

import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.formatters.CoordinateFormatter
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoordinateInput @Inject constructor() : TextInput, Input.HasRandomUri {
    override val pattern = Regex("""([\d.\-\p{Zs},°'′"″NSWE]*\d[\d.\-\p{Zs},°'′"″NSWE]*)""")
    override val documentation = InputDocumentation(
        group = InputDocumentationGroup.COORDINATES,
        items = listOf(
            InputDocumentationItem.Text(20) {
                stringResource(
                    R.string.example, CoordinateFormatter.formatDegMinSecCoords(WGS84Point(NaivePoint.example))
                )
            },
        ),
    )

    override suspend fun parse(data: String, match: String) = parseResult {
        // Decimal
        // e.g. `N 41.40338, E 2.17403`
        Regex("""$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LON_SIG$LON_DEG$CHARS*""")
            .matchEntire(data)
            ?.let { m ->
                points = persistentListOf(
                    WGS84Point(
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
                        source = Source.TEXT,
                    )
                )
                return@parseResult
            }

        // Degrees minutes seconds
        // e.g. `41°24'12.2"N 2°10'26.5"E`
        Regex("""$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LAT_MIN$CHARS+$LAT_SEC$CHARS+$SPACE$LON_SIG$LON_DEG$CHARS+$LON_MIN$CHARS+$LON_SEC$CHARS*""")
            .matchEntire(data)
            ?.let { m ->
                points = persistentListOf(
                    WGS84Point(
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
                        source = Source.TEXT,
                    )
                )
                return@parseResult
            }

        // Degrees minutes
        // e.g. `41 24.2028, 2 10.4418`
        Regex("""$CHARS*$LAT_SIG$LAT_DEG$CHARS+$LAT_MIN$CHARS+$LON_SIG$LON_DEG$CHARS+$LON_MIN$CHARS*""")
            .matchEntire(data)
            ?.let { m ->
                points = persistentListOf(
                    WGS84Point(
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
                        source = Source.TEXT,
                    )
                )
                return@parseResult
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

    override fun genRandomUri(point: Point) =
        UriFormatter.formatUriString(point, "N {lat}, E {lon}")

    override fun toString() = "CoordinateInput"

    private companion object {
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
    }
}
