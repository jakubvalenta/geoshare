package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matches
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Srs
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
    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        on { path matches "/l/$LAT,$LON" } doReturn { PositionMatch(it, srs) }
        on { if (path == "/") queryParams["map"]?.let { it matches "$LAT,$LON,$Z" } else null } doReturn
                { PositionMatch(it, srs) }
        all {
            optional {
                on { queryParams["map"]?.let { it matches "$LAT,$LON,$Z" } } doReturn { PositionMatch(it, srs) }
            }
            on { path matches """/p/[a-z]-(?P<encoded>$SIMPLIFIED_BASE64)""" } doReturn
                    { EncodedPositionMatch(it, srs) }
        }
    }

    private class EncodedPositionMatch(matcher: Matcher, srs: Srs) : PositionMatch(matcher, srs) {
        var decodedLatPatternCache: Pattern? = null
        val decodedLatPattern: Pattern
            get() = decodedLatPatternCache ?: Pattern.compile("""(lat=|"latitude":)$LAT""")
                .also { decodedLatPatternCache = it }
        var decodedLonPatternCache: Pattern? = null
        val decodedLonPattern: Pattern
            get() = decodedLonPatternCache ?: Pattern.compile("""(lon=|"longitude":)$LON""")
                .also { decodedLonPatternCache = it }

        override val points: List<Point>?
            get() {
                val encoded = matcher.groupOrNull("encoded") ?: return null
                val decoded = Base64.decode(encoded).decodeToString()
                val lat = decodedLatPattern.matcher(decoded)?.takeIf { it.find() }?.groupOrNull("lat")?.toDoubleOrNull()
                    ?: return null
                val lon = decodedLonPattern.matcher(decoded)?.takeIf { it.find() }?.groupOrNull("lon")?.toDoubleOrNull()
                    ?: return null
                return persistentListOf(Point(srs, lat, lon))
            }
    }
}
