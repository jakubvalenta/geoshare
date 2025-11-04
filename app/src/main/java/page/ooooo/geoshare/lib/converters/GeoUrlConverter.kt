package page.ooooo.geoshare.lib.converters

import androidx.compose.ui.res.stringResource
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.PositionMatch
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.conversionPattern
import page.ooooo.geoshare.lib.matches
import page.ooooo.geoshare.lib.outputs.GeoUriOutput

class GeoUrlConverter : UrlConverter.WithUriPattern {
    override val uriPattern: Pattern = Pattern.compile("""geo:\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_geo_name, inputs = listOf(
            DocumentationInput.Text(3) {
                stringResource(R.string.example, GeoUriOutput.formatUriString(Position.example))
            },
        )
    )

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        all {
            optional {
                on { path matches """$LAT,$LON""" } doReturn { PositionMatch(it) }
            }
            optional {
                on { queryParams["q"]?.let { it matches Q_PARAM } } doReturn { PositionMatch(it) }
            }
            optional {
                on { queryParams["z"]?.let { it matches Z } } doReturn { PositionMatch(it) }
            }
        }
    }
}
