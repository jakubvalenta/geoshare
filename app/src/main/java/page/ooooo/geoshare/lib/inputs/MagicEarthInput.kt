package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.*
import page.ooooo.geoshare.lib.position.LatLonZ
import page.ooooo.geoshare.lib.position.PositionBuilder
import page.ooooo.geoshare.lib.position.Srs

/**
 * See https://web.archive.org/web/20250609044205/https://www.magicearth.com/developers/
 */
object MagicEarthInput : Input {
    const val NAME = "Magic Earth"

    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""((https?://)?magicearth.com|magicearth:/)/\?\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_magic_earth_name,
        inputs = listOf(
            Input.DocumentationInput.Url(20, "https://magicearth.com/"),
        ),
    )

    override fun parseUri(uri: Uri) = uri.run {
        PositionBuilder(srs).apply {
            setPointIfEmpty {
                (LAT_PATTERN match queryParams["lat"])?.toLat()?.let { lat ->
                    (LON_PATTERN match queryParams["lon"])?.toLon()?.let { lon ->
                        LatLonZ(lat, lon, null)
                    }
                }
            }
            setPointIfEmpty { LAT_LON_PATTERN matchLatLonZ queryParams["name"] }
            setQOrNameIfEmpty { Q_PARAM_PATTERN matchQ queryParams["name"] }
            @Suppress("SpellCheckingInspection")
            setQIfEmpty { Q_PARAM_PATTERN matchQ queryParams["daddr"] }
            setQOrNameIfEmpty { Q_PARAM_PATTERN matchQ queryParams["q"] }
            setZIfEmpty { Z_PATTERN matchZ queryParams["z"] }
            setZIfEmpty { Z_PATTERN matchZ queryParams["zoom"] }
        }.toPair()
    }
}
