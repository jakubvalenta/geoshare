package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Matcher
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.IncompletePosition
import page.ooooo.geoshare.lib.PositionMatch
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.conversionPattern
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.matches
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.toIncompleteLatLonPosition
import page.ooooo.geoshare.lib.toIncompleteLatLonZPosition
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
    override val conversionUriPattern = conversionPattern<Uri, IncompletePosition> {
        on { (path matches "/l/$LAT,$LON")?.toIncompleteLatLonPosition(srs) }
        on {
            if (path == "/") queryParams["map"]?.let { it matches "$LAT,$LON,$Z" }
                ?.toIncompleteLatLonZPosition(srs) else null
        }
        all {
            optional {
                on { queryParams["map"]?.let { it matches ".*,$Z" } } doReturn { PositionMatch.Zoom(it, srs) }
            }
            on { path matches """/p/[a-z]-(?P<encoded>$SIMPLIFIED_BASE64)""" } doReturn
                { EncodedPositionMatch(it, srs) }
        }
    }

    private class EncodedPositionMatch(matcher: Matcher, srs: Srs) : PositionMatch.LatLon(matcher, srs) {
        override val latLon: Pair<Double, Double>?
            get() {
                val encoded = matcher.groupOrNull("encoded") ?: return null
                val decoded = Base64.decode(encoded).decodeToString()
                val lat = (decoded find """(lat=|"latitude":)$LAT""")?.groupOrNull("lat")?.toDoubleOrNull()
                    ?: return null
                val lon = (decoded find """(lon=|"longitude":)$LON""")?.groupOrNull("lon")?.toDoubleOrNull()
                    ?: return null
                return Pair(lat, lon)
            }
    }
}
