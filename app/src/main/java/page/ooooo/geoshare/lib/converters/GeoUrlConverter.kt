package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.PositionRegex
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import page.ooooo.geoshare.lib.uriPattern
import page.ooooo.geoshare.R

class GeoUrlConverter : UrlConverter.WithUriPattern {
    @StringRes
    override val nameResId = R.string.converter_geo_name

    override val uriPattern: Pattern = Pattern.compile("""geo:\S+""")
    override val supportedUriStrings = listOf(
        "geo:",
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
