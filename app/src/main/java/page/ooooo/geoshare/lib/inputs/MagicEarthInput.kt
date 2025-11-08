package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.PositionMatch
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.conversionPattern
import page.ooooo.geoshare.lib.extensions.matches

/**
 * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/
 */
object MagicEarthInput : Input.HasUri {
    const val NAME = "Magic Earth"

    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""((https?://)?magicearth.com|magicearth:/)/\?\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_magic_earth_name,
        inputs = listOf(
            Input.DocumentationInput.Url(20, "https://magicearth.com/"),
        ),
    )

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        all {
            optional {
                on { queryParams["z"]?.let { it matches Z } } doReturn { PositionMatch(it, srs) }
            }
            optional {
                on { queryParams["zoom"]?.let { it matches Z } } doReturn { PositionMatch(it, srs) }
            }
            first {
                all {
                    on { queryParams["lat"]?.let { it matches LAT } } doReturn { PositionMatch(it, srs) }
                    on { queryParams["lon"]?.let { it matches LON } } doReturn { PositionMatch(it, srs) }
                }
                on { queryParams["name"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it, srs) }
                @Suppress("SpellCheckingInspection")
                on { queryParams["daddr"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it, srs) }
                on { queryParams["q"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it, srs) }
            }
        }
    }
}
