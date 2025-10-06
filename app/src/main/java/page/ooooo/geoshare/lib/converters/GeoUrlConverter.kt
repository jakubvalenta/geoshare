package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.PositionRegex
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import page.ooooo.geoshare.lib.uriPattern

class GeoUrlConverter : UrlConverter.WithUriPattern {
    override val uriPattern: Pattern = Pattern.compile("""geo:\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_geo_name,
        inputs = listOf(
            DocumentationInput.Text(R.string.converter_geo_example, 3),
        )
    )
    override val conversionUriPattern = uriPattern {
        all {
            optional {
                path(PositionRegex("""$LAT,$LON"""))
            }
            optional {
                query("q", PositionRegex(Q_PARAM))
            }
            optional {
                query("z", PositionRegex(Z))
            }
        }
    }
}
