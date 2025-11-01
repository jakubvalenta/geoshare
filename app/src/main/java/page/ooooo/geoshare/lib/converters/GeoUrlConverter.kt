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
import page.ooooo.geoshare.lib.conversionPattern
import page.ooooo.geoshare.lib.matcherIfMatches

class GeoUrlConverter : UrlConverter.WithUriPattern {
    override val uriPattern: Pattern = Pattern.compile("""geo:\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_geo_name,
        inputs = listOf(
            DocumentationInput.Text(3) {
                stringResource(R.string.example, Position.example.toGeoUriString())
            },
        )
    )
    override val conversionUriPattern = conversionPattern {
        all {
            optional {
                onUri { path matcherIfMatches """$LAT,$LON""" } doReturn { PositionMatch(it) }
            }
            optional {
                onUri { queryParams["q"]?.let { it matcherIfMatches Q_PARAM } } doReturn { PositionMatch(it) }
            }
            optional {
                onUri { queryParams["z"]?.let { it matcherIfMatches Z } } doReturn { PositionMatch(it) }
            }
        }
    }
}
