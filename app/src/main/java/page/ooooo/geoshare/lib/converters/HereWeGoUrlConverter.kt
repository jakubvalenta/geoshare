package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.PositionRegex
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import page.ooooo.geoshare.lib.uriPattern
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class HereWeGoUrlConverter() : UrlConverter.WithUriPattern {
    companion object {
        const val SIMPLIFIED_BASE64 = """[A-Za-z0-9+/]+=*"""
        val DECODED_LAT_PATTERN: Pattern = Pattern.compile("""(lat=|"latitude":)$LAT""")
        val DECODED_LON_PATTERN: Pattern = Pattern.compile("""(lon=|"longitude":)$LON""")
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
            path(object : PositionRegex("""/p/[a-z]-(?P<encoded>$SIMPLIFIED_BASE64)""") {
                override val points: List<Point>?
                    get() {
                        val encoded = groupOrNull("encoded") ?: return null
                        val decoded = Base64.decode(encoded).decodeToString()
                        val lat = DECODED_LAT_PATTERN.matcher(decoded)?.takeIf { it.find() }?.let { m ->
                            try {
                                m.group("lat")
                            } catch (_: IllegalArgumentException) {
                                null
                            }
                        } ?: return null
                        val lon = DECODED_LON_PATTERN.matcher(decoded)?.takeIf { it.find() }?.let { m ->
                            try {
                                m.group("lon")
                            } catch (_: IllegalArgumentException) {
                                null
                            }
                        } ?: return null
                        return listOf(lat to lon)
                    }
            })
        }
    }
}
