package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.allHtmlPattern
import page.ooooo.geoshare.lib.allUrlPattern

class YandexMapsUrlConverter() : UrlConverter {
    override val name = "Yandex Maps"

    override val host: Pattern = Pattern.compile("""yandex.com""")
    override val shortUrlPattern: Pattern = Pattern.compile("""https://yandex\.com/maps/-/.+""")

    override val urlPattern = allUrlPattern {
        optional {
            query("z", z, sanitizeZoom)
        }
        first {
            query("ll", "$lon,$lat")
            path("""/maps/org/\d+/.*""")
        }
    }

    override val htmlPattern = allHtmlPattern {
        html(""".*?data-coordinates="$lon,$lat".*""")
    }
    override val htmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_yandex_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_yandex_maps_loading_indicator_title
}
