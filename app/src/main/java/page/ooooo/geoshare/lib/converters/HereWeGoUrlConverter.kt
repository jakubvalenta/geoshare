package page.ooooo.geoshare.lib.converters

import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class HereWeGoUrlConverter() : UrlConverter.WithUriPattern {
    companion object {
        const val SIMPLIFIED_BASE64 = """[A-Za-z0-9+/]+=*"""
    }

    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""(https?://)?(share|wego)\.here\.com/\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_here_wego_name,
        inputs = listOf(
            DocumentationInput.Url(20, "https://share.here.com/l/"),
            DocumentationInput.Url(20, "https://share.here.com/p/"),
            DocumentationInput.Url(20, "https://wego.here.com/"),
            DocumentationInput.Url(20, "https://wego.here.com/p/"),
        ),
    )

    @OptIn(ExperimentalEncodingApi::class)
    override val conversionUriPattern = conversionPattern {
        path("/l/$LAT,$LON") { PositionMatch(it) }
        all {
            path("/") { PositionMatch(it) }
            query("map", "$LAT,$LON,$Z") { PositionMatch(it) }
        }
        all {
            optional {
                query("map", "$LAT,$LON,$Z") { PositionMatch(it) }
            }
            path("""/p/[a-z]-(?P<encoded>$SIMPLIFIED_BASE64)""") { EncodedPositionMatch(it) }
        }
    }

    private class EncodedPositionMatch(matcher: Matcher) : PositionMatch(matcher) {
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
                val lat = decodedLatPattern.matcherIfFind(decoded)?.groupOrNull("lat") ?: return null
                val lon = decodedLonPattern.matcherIfFind(decoded)?.groupOrNull("lon") ?: return null
                return persistentListOf(Point(lat, lon))
            }
    }
}
