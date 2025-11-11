package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ConversionPattern
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LAT_PATTERN
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LON_PATTERN
import page.ooooo.geoshare.lib.ConversionPattern.Companion.Q_PARAM_PATTERN
import page.ooooo.geoshare.lib.ConversionPattern.Companion.Z_PATTERN
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.position.toQ
import page.ooooo.geoshare.lib.position.toZ

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

    override val conversionUriPattern = ConversionPattern.first<Uri, Position> {
        all {
            optional {
                first {
                    pattern { (Z_PATTERN match queryParams["z"])?.toZ(srs) }
                    pattern { (Z_PATTERN match queryParams["zoom"])?.toZ(srs) }
                }
            }
            first {
                pattern {
                    (LAT_PATTERN match queryParams["lat"])?.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
                        (LON_PATTERN match queryParams["lon"])?.groupOrNull("lon")?.toDoubleOrNull()?.let { lon ->
                            Position(srs, lat, lon)
                        }
                    }
                }
                pattern { (Q_PARAM_PATTERN match queryParams["name"])?.toQ(srs) }
                @Suppress("SpellCheckingInspection")
                pattern { (Q_PARAM_PATTERN match queryParams["daddr"])?.toQ(srs) }
                pattern { (Q_PARAM_PATTERN match queryParams["q"])?.toQ(srs) }
            }
        }
    }
}
