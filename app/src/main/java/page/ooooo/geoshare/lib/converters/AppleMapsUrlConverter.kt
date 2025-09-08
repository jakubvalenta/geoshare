package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.allHtmlPattern
import page.ooooo.geoshare.lib.allUrlPattern

class AppleMapsUrlConverter() : UrlConverter {
    override val name = "Apple Maps"

    override val host: Pattern = Pattern.compile("""maps\.apple(\.com)?""")
    override val shortUrlHost = null

    @Suppress("SpellCheckingInspection")
    override val urlPattern = allUrlPattern {
        query("z", z)
        first {
            all {
                host("maps.apple")
                path("/p/.+")
            }
            query("ll", "$lat,$lon")
            query("coordinate", "$lat,$lon")
            query("q", "$lat,$lon")
            query("address", q)
            query("name", q)
            all {
                query("auid", ".+")
                query("q", q)
            }
            all {
                query("place-id", ".+")
                query("q", q)
            }
            query("auid", ".+")
            query("place-id", ".+")
            all {
                query("q", q)
                query("sll", "$lat,$lon")
            }
            query("q", q)
            query("sll", "$lat,$lon")
            query("center", "$lat,$lon")
        }
    }

    override val htmlPattern = allHtmlPattern {
        html("""<meta property="place:location:latitude" content="$lat"""")
        html("""<meta property="place:location:longitude" content="$lon"""")
    }

    override val htmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_apple_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_apple_maps_loading_indicator_title
}
