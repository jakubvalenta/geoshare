package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.*
import page.ooooo.geoshare.lib.position.LatLonZ
import page.ooooo.geoshare.lib.position.PositionBuilder
import page.ooooo.geoshare.lib.position.Srs
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object HereWeGoInput : Input {
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
    override fun parseUri(uri: Uri) = uri.run {
        PositionBuilder(srs).apply {
            setPointIfNull { """/l/$LAT,$LON""" matchLatLonZ path }
            setPointIfNull { if (path == "/") ("""$LAT,$LON,$Z""" matchLatLonZ queryParams["map"]) else null }
            setPointIfNull {
                ("""/p/[a-z]-(?P<encoded>$SIMPLIFIED_BASE64)""" match path)?.groupOrNull("encoded")?.let { encoded ->
                    Base64.decode(encoded).decodeToString().let { decoded ->
                        ("""(lat=|"latitude":)$LAT""" find decoded)?.toLat()?.let { lat ->
                            ("""(lon=|"longitude":)$LON""" find decoded)?.toLon()?.let { lon ->
                                LatLonZ(lat, lon, null)
                            }
                        }
                    }
                }
            }
            setZIfNull { """.*,$Z""" matchZ queryParams["map"] }
        }.toPair()
    }
}
