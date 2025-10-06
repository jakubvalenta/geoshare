package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.PositionRegex
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Q_PARAM
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import page.ooooo.geoshare.lib.uriPattern

/**
 * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/
 */
class MagicEarthUrlConverter : UrlConverter.WithUriPattern {
    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""(https?://)?magicearth.com/\?\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_magic_earth_name,
        inputs = listOf(
            DocumentationInput.Url("https://magicearth.com/", 20),
        ),
    )

    override val conversionUriPattern = uriPattern {
        all {
            optional {
                query("z", PositionRegex(Z))
            }
            first {
                all {
                    query("lat", PositionRegex(LAT))
                    query("lon", PositionRegex(LON))
                }
                query("name", PositionRegex(Q_PARAM))
                @Suppress("SpellCheckingInspection")
                query("daddr", PositionRegex(Q_PARAM))
                query("q", PositionRegex(Q_PARAM))
            }
        }
    }
}
