package page.ooooo.geoshare.lib.inputs

import androidx.compose.ui.res.stringResource
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.PositionMatch
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.conversionPattern
import page.ooooo.geoshare.lib.extensions.matches
import page.ooooo.geoshare.lib.outputs.GeoUriOutputGroup

object GeoInput : Input.HasUri {
    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""geo:\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_geo_name, inputs = listOf(
            Input.DocumentationInput.Text(3) {
                stringResource(R.string.example, GeoUriOutputGroup.formatUriString(Position.example, Srs.WGS84))
            },
        )
    )

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        all {
            optional {
                on { path matches """$LAT,$LON""" } doReturn { PositionMatch.LatLon(it, srs) }
            }
            optional {
                on { queryParams["q"]?.let { it matches Q_PARAM } } doReturn { PositionMatch.Query(it, srs) }
            }
            optional {
                on { queryParams["z"]?.let { it matches Z } } doReturn { PositionMatch.Zoom(it, srs) }
            }
        }
    }
}
