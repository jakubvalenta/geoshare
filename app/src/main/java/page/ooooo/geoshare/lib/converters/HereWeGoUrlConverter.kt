package page.ooooo.geoshare.lib.converters

import com.google.re2j.Pattern
import page.ooooo.geoshare.lib.allUriPattern
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class HereWeGoUrlConverter() : UrlConverter.WithUriPattern {
    @Suppress("SpellCheckingInspection")
    override val uriPattern: Pattern = Pattern.compile("""https?://(share|wego)\.here\.com/\S+""")

    @OptIn(ExperimentalEncodingApi::class)
    override val conversionUriPattern = allUriPattern {
        val simpleBase64Regex = """[A-Za-z0-9+/]+=*"""
        val coordStringLatPattern = Pattern.compile("""(lat=|"latitude":)$lat""")
        val coordStringLonPattern = Pattern.compile("""(lon=|"longitude":)$lon""")

        first {
            path("/l/$lat,$lon")
            all {
                path("/")
                query("map", "$lat,$lon,$z", sanitizeZoom)
            }
            all {
                optional {
                    query("map", "$lat,$lon,$z", sanitizeZoom)
                }
                path("""/p/[a-z]-(?P<encoded>$simpleBase64Regex)""") { name, value ->
                    if (name == "lat" || name == "lon") {
                        val encoded = groupOrNull(matcher, "encoded")
                        if (encoded != null) {
                            val decoded = Base64.decode(encoded).decodeToString()
                            val pattern = if (name == "lat") coordStringLatPattern else coordStringLonPattern
                            val m = pattern.matcher(decoded)
                            if (m.find()) {
                                groupOrNull(m, name)
                            } else {
                                value
                            }
                        } else {
                            value
                        }
                    } else {
                        value
                    }
                }
            }
        }
    }
}
