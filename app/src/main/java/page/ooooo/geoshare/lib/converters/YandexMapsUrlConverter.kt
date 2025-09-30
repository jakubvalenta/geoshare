package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.PositionRegex
import page.ooooo.geoshare.lib.PositionRegex.Companion.LAT
import page.ooooo.geoshare.lib.PositionRegex.Companion.LON
import page.ooooo.geoshare.lib.PositionRegex.Companion.Z
import page.ooooo.geoshare.lib.htmlPattern
import page.ooooo.geoshare.lib.uriPattern

class YandexMapsUrlConverter() :
    UrlConverter.WithUriPattern,
    UrlConverter.WithShortUriPattern,
    UrlConverter.WithHtmlPattern {

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?yandex(\.[a-z]{2,3})?\.[a-z]{2,3}/\S+""")
    override val shortUriPattern: Pattern =
        Pattern.compile("""(https?://)?yandex(\.[a-z]{2,3})?\.[a-z]{2,3}/maps/-/\S+""")

    @Suppress("SpellCheckingInspection")
    override val conversionUriPattern = uriPattern {
        all {
            optional {
                first {
                    query("whatshere%5Bzoom%5D", PositionRegex(Z))
                    query("z", PositionRegex(Z))
                }
            }
            first {
                query("whatshere%5Bpoint%5D", PositionRegex("$LON,$LAT"))
                query("ll", PositionRegex("$LON,$LAT"))
                path(PositionRegex("""/maps/org/\d+([/?#].*|$)"""))
            }
        }
    }

    override val conversionHtmlPattern = htmlPattern {
        content(PositionRegex("""data-coordinates="$LON,$LAT""""))
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_yandex_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_yandex_maps_loading_indicator_title
}
