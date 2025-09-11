package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.allHtmlPattern
import page.ooooo.geoshare.lib.allUriPattern

class YandexMapsUrlConverter() : UrlConverter.WithUriPattern, UrlConverter.WithShortUriPattern,
    UrlConverter.WithHtmlPattern {
    override val uriPattern: Pattern = Pattern.compile("""https?://yandex\.com/\S+""")
    override val shortUriPattern: Pattern = Pattern.compile("""https?://yandex\.com/maps/-/\S+""")

    override val conversionUriPattern = allUriPattern {
        optional {
            query("z", z, sanitizeZoom)
        }
        first {
            query("ll", "$lon,$lat")
            path("""/maps/org/\d+/.*""")
        }
    }

    override val conversionHtmlPattern = allHtmlPattern {
        html(""".*?data-coordinates="$lon,$lat".*""")
    }
    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_yandex_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_yandex_maps_loading_indicator_title
}
