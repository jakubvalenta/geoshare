package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.allUrlPattern

class MagicEarthUrlConverter : UrlConverter {
    override val name = "Magic Earth"

    @Suppress("SpellCheckingInspection")
    override val host: Pattern = Pattern.compile("""(share|wego)\.here\.com""")
    override val shortUrlPattern = null

    override val urlPattern = allUrlPattern {
        optional {
            query("z", z, sanitizeZoom)
        }
        first {
            all {
                query("lat", lat)
                query("lon", lon)
            }
            query("name", q)
            query("q", q)
            @Suppress("SpellCheckingInspection")
            query("daddr", q)
        }
    }

    override val htmlPattern = null
    override val htmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_magic_earth_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_magic_earth_loading_indicator_title
}
