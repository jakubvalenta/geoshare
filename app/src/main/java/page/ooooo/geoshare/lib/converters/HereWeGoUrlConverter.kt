package page.ooooo.geoshare.lib.converters

import com.google.re2j.Matcher
import com.google.re2j.Pattern
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.*
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class HereWeGoUrlConverter() : UrlConverter.WithUriPattern {
    companion object {
        const val SIMPLIFIED_BASE64 = """[A-Za-z0-9+/]+=*"""
        val DECODED_LAT_PATTERN: Pattern = Pattern.compile("""(lat=|"latitude":)$LAT""")
        val DECODED_LON_PATTERN: Pattern = Pattern.compile("""(lon=|"longitude":)$LON""")
    }

    class EncodedPositionMatch(matcher: Matcher) : PositionMatch(matcher) {
        override val points: List<Point>?
            get() {
                val encoded = matcher.groupOrNull("encoded") ?: return null
                val decoded = Base64.decode(encoded).decodeToString()
                val lat = DECODED_LAT_PATTERN.matcherIfFind(decoded)?.groupOrNull("lat") ?: return null
                val lon = DECODED_LON_PATTERN.matcherIfFind(decoded)?.groupOrNull("lon") ?: return null
                return persistentListOf(Point(lat, lon))
            }
    }

    class EncodedPositionRegex(regex: String) : PositionRegex(regex) {
        override fun matches(input: String) = pattern.matcherIfMatches(input)?.let { EncodedPositionMatch(it) }
        override fun find(input: String) = pattern.matcherIfFind(input)?.let { EncodedPositionMatch(it) }
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
    override val conversionUriPattern = uriPattern {
        path(PositionRegex("/l/$LAT,$LON"))
        all {
            path(PositionRegex("/"))
            query("map", PositionRegex("$LAT,$LON,$Z"))
        }
        all {
            optional {
                query("map", PositionRegex("$LAT,$LON,$Z"))
            }
            path(EncodedPositionRegex("""/p/[a-z]-(?P<encoded>$SIMPLIFIED_BASE64)"""))
        }
    }
}
