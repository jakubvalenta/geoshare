package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.allUriPattern

class MagicEarthUrlConverter : UrlConverter {
    override val name = "Magic Earth"

    override val uriPattern: Pattern = Pattern.compile("""magicearth://\?.+""")
    override val shortUriPattern = null

    override val conversionUriPattern = allUriPattern {
        optional {
            query("z", z, sanitizeZoom)
        }
        first {
            all {
                query("lat", lat)
                query("lon", lon)
            }
            query("name", q)
            @Suppress("SpellCheckingInspection")
            query("daddr", q)
            query("q", q)
        }
    }

    override val conversionHtmlPattern = null
    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_magic_earth_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_magic_earth_loading_indicator_title
}
