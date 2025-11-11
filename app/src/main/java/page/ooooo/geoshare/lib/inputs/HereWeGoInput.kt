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
import page.ooooo.geoshare.lib.extensions.matches
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
        pattern { (path matches "/l/$LAT,$LON")?.toLatLon(srs) }
        pattern { queryParams["map"]?.takeIf { path == "/" }?.let { it matches "$LAT,$LON,$Z" }?.toLatLonZ(srs) }
        all {
            optional {
                pattern { queryParams["map"]?.let { it matches ".*,$Z" }?.toZ(srs) }
            }
            pattern {
                (path matches """/p/[a-z]-(?P<encoded>$SIMPLIFIED_BASE64)""")?.groupOrNull("encoded")?.let { encoded ->
                    Base64.decode(encoded).decodeToString().let { decoded ->
                        (decoded find """(lat=|"latitude":)$LAT""")?.groupOrNull("lat")?.toDoubleOrNull()?.let { lat ->
                            (decoded find """(lon=|"longitude":)$LON""")?.groupOrNull("lon")?.toDoubleOrNull()
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
