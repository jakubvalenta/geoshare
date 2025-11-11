package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ConversionPattern
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LAT
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LON
import page.ooooo.geoshare.lib.ConversionPattern.Companion.Z
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.position.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object HereWeGoInput : Input.HasUri {
    private const val SIMPLIFIED_BASE64 = """[A-Za-z0-9+/]+=*"""

    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(share|wego)\.here\.com/\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_here_wego_name,
        inputs = listOf(
            Input.DocumentationInput.Url(20, "https://share.here.com/l/"),
            Input.DocumentationInput.Url(20, "https://share.here.com/p/"),
            Input.DocumentationInput.Url(20, "https://wego.here.com/"),
            Input.DocumentationInput.Url(20, "https://wego.here.com/p/"),
        ),
    )

    @OptIn(ExperimentalEncodingApi::class)
    override val conversionUriPattern = ConversionPattern.first<Uri, Position> {
        pattern { ("""/l/$LAT,$LON""" match path)?.toLatLon(srs) }
        pattern { if (path == "/") ("""$LAT,$LON,$Z""" match queryParams["map"])?.toLatLonZ(srs) else null }
        all {
            optional {
                pattern { (""".*,$Z""" match queryParams["map"])?.toZ(srs) }
            }
            pattern {
                ("""/p/[a-z]-(?P<encoded>$SIMPLIFIED_BASE64)""" match path)?.groupOrNull("encoded")
                    ?.let { encoded ->
                        Base64.decode(encoded).decodeToString().let { decoded ->
                            ("""(lat=|"latitude":)$LAT""" find decoded)?.groupOrNull("lat")
                                ?.toDoubleOrNull()
                                ?.let { lat ->
                                    ("""(lon=|"longitude":)$LON""" find decoded)?.groupOrNull("lon")
                                        ?.toDoubleOrNull()
                                        ?.let { lon ->
                                            Position(srs, lat, lon)
                                        }
                                }
                        }
                    }
            }
        }
    }
}
