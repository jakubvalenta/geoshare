package page.ooooo.geoshare.lib.inputs

import androidx.compose.ui.res.stringResource
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.conversion.ConversionPattern
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.LAT_LON_PATTERN
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.Q_PARAM_PATTERN
import page.ooooo.geoshare.lib.conversion.ConversionPattern.Companion.Z_PATTERN
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.outputs.GeoUriOutputGroup
import page.ooooo.geoshare.lib.position.*

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

    override val conversionUriPattern = ConversionPattern.first<Uri, Position> {
        all {
            optional {
                pattern { (LAT_LON_PATTERN match path)?.toLatLon(srs) }
            }
            optional {
                pattern { (Q_PARAM_PATTERN match queryParams["q"])?.toQ(srs) }
            }
            optional {
                pattern { (Z_PATTERN match queryParams["z"])?.toZ(srs) }
            }
        }
    }
}
