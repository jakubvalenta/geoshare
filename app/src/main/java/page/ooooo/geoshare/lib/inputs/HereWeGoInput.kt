package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.extensions.matchNaivePoint
import page.ooooo.geoshare.lib.extensions.matchZ
import page.ooooo.geoshare.lib.extensions.toLat
import page.ooooo.geoshare.lib.extensions.toLon
import page.ooooo.geoshare.lib.point.NaivePoint
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object HereWeGoInput : Input {
    private const val SIMPLIFIED_BASE64 = """[A-Za-z0-9+/]+=*"""

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(share|wego)\.here\.com/\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.HERE_WEGO,
        nameResId = R.string.converter_here_wego_name,
        items = listOf(
            InputDocumentationItem.Url(20, "https://share.here.com/l/"),
            InputDocumentationItem.Url(20, "https://share.here.com/p/"),
            InputDocumentationItem.Url(20, "https://wego.here.com/"),
            InputDocumentationItem.Url(20, "https://wego.here.com/p/"),
        ),
    )

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun parseUri(uri: Uri): ParseUriResult? {
        val points = buildPoints {
            uri.run {
                setPointIfNull { """/l/$LAT,$LON""" matchNaivePoint path }
                setPointIfNull { if (path == "/") ("""$LAT,$LON,$Z""" matchNaivePoint queryParams["map"]) else null }
                setPointIfNull {
                    ("""/p/[a-z]-(?P<encoded>$SIMPLIFIED_BASE64)""" match path)?.groupOrNull("encoded")
                        ?.let { encoded ->
                            Base64.decode(encoded).decodeToString().let { decoded ->
                                ("""(lat=|"latitude":)$LAT""" find decoded)?.toLat()?.let { lat ->
                                    ("""(lon=|"longitude":)$LON""" find decoded)?.toLon()?.let { lon ->
                                        NaivePoint(lat, lon)
                                    }
                                }
                            }
                        }
                }
                setZIfNull { """.*,$Z""" matchZ queryParams["map"] }
            }
        }
        return ParseUriResult.from(points.asWGS84())
    }
}
