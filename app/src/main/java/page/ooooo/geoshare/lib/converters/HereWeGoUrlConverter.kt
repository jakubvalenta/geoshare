package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.allUrlPattern
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class HereWeGoUrlConverter() : UrlConverter {
    override val name = "HERE WeGo"

    @Suppress("SpellCheckingInspection")
    override val host: Pattern = Pattern.compile("""(share|wego)\.here\.com""")
    override val shortUrlHost = null

    @OptIn(ExperimentalEncodingApi::class)
    override val urlPattern = allUrlPattern {
        val simpleBase64Regex = """[A-Za-z0-9+/]+=*"""
        val coordStringLatPattern = Pattern.compile("""(lat=|"latitude":)$lat""")
        val coordStringLonPattern = Pattern.compile("""(lon=|"longitude":)$lon""")

        first {
            all {
                path("/")
                query("map", "$lat,$lon,$z", sanitizeZoom)
            }
            all {
                optional {
                    query("map", "$lat,$lon,$z", sanitizeZoom)
                }
                path("""/p/[a-z]-(?P<lat>$simpleBase64Regex)""") { name, value ->
                    if (name == "lat" && value != null) {
                        val decoded = Base64.decode(value).decodeToString()
                        val m = coordStringLatPattern.matcher(decoded)?.takeIf { it.find() }
                        try {
                            m?.group("lat")
                        } catch (_: IllegalArgumentException) {
                            null
                        }
                    } else {
                        value
                    }
                }
                path("""/p/[a-z]-(?P<lon>$simpleBase64Regex)""") { name, value ->
                    if (name == "lon" && value != null) {
                        val decoded = Base64.decode(value).decodeToString()
                        val m = coordStringLonPattern.matcher(decoded)?.takeIf { it.find() }
                        try {
                            m?.group("lon")
                        } catch (_: IllegalArgumentException) {
                            null
                        }
                    } else {
                        value
                    }
                }
            }
        }
    }

    override val htmlPattern = null
    override val htmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_here_we_go_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_here_we_go_loading_indicator_title
}
