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
                path("""$LAT,$LON""") { PositionMatch(it) }
            }
            optional {
                query("q", Q_PARAM) { PositionMatch(it) }
            }
            optional {
                query("z", Z) { PositionMatch(it) }
            }
        }
    }
}
