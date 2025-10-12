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
    override val documentation = Documentation(
        nameResId = R.string.converter_yandex_maps_name,
        inputs = listOf(
            DocumentationInput.Url("https://ya.ru/maps", 20),
            DocumentationInput.Url("https://yandex.az/maps", 22),
            DocumentationInput.Url("https://yandex.by/maps", 22),
            DocumentationInput.Url("https://yandex.co.il/maps", 22),
            DocumentationInput.Url("https://yandex.com/maps", 20),
            DocumentationInput.Url("https://yandex.com.am/maps", 22),
            DocumentationInput.Url("https://yandex.com.ge/maps", 22),
            DocumentationInput.Url("https://yandex.com.tr/maps", 22),
            DocumentationInput.Url("https://yandex.ee/maps", 22),
            DocumentationInput.Url("https://yandex.eu/maps", 22),
            DocumentationInput.Url("https://yandex.fr/maps", 22),
            DocumentationInput.Url("https://yandex.kg/maps", 22),
            DocumentationInput.Url("https://yandex.kz/maps", 22),
            DocumentationInput.Url("https://yandex.lt/maps", 22),
            DocumentationInput.Url("https://yandex.lv/maps", 22),
            DocumentationInput.Url("https://yandex.md/maps", 22),
            DocumentationInput.Url("https://yandex.ru/maps", 22),
            DocumentationInput.Url("https://yandex.tj/maps", 22),
            DocumentationInput.Url("https://yandex.tm/maps", 22),
            DocumentationInput.Url("https://yandex.ua/maps", 22),
            DocumentationInput.Url("https://yandex.uz/maps", 22),
        ),
    )
    override val shortUriPattern: Pattern =
        Pattern.compile("""(https?://)?yandex(\.[a-z]{2,3})?\.[a-z]{2,3}/maps/-/\S+""")
    override val shortUriMethod: ShortUriMethod = ShortUriMethod.HEAD

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

    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_yandex_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_yandex_maps_loading_indicator_title
}
