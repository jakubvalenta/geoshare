package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.PositionMatch
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.conversionPattern
import page.ooooo.geoshare.lib.matcherIfFind
import page.ooooo.geoshare.lib.matcherIfMatches

class YandexMapsUrlConverter() :
    UrlConverter.WithUriPattern,
    UrlConverter.WithShortUriPattern,
    UrlConverter.WithHtmlPattern {

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?yandex(\.[a-z]{2,3})?\.[a-z]{2,3}/\S+""")
    override val documentation = Documentation(
        nameResId = R.string.converter_yandex_maps_name,
        inputs = listOf(
            DocumentationInput.Url(20, "https://ya.ru/maps"),
            DocumentationInput.Url(22, "https://yandex.az/maps"),
            DocumentationInput.Url(22, "https://yandex.by/maps"),
            DocumentationInput.Url(22, "https://yandex.co.il/maps"),
            DocumentationInput.Url(20, "https://yandex.com/maps"),
            DocumentationInput.Url(22, "https://yandex.com.am/maps"),
            DocumentationInput.Url(22, "https://yandex.com.ge/maps"),
            DocumentationInput.Url(22, "https://yandex.com.tr/maps"),
            DocumentationInput.Url(22, "https://yandex.ee/maps"),
            DocumentationInput.Url(22, "https://yandex.eu/maps"),
            DocumentationInput.Url(22, "https://yandex.fr/maps"),
            DocumentationInput.Url(22, "https://yandex.kg/maps"),
            DocumentationInput.Url(22, "https://yandex.kz/maps"),
            DocumentationInput.Url(22, "https://yandex.lt/maps"),
            DocumentationInput.Url(22, "https://yandex.lv/maps"),
            DocumentationInput.Url(22, "https://yandex.md/maps"),
            DocumentationInput.Url(22, "https://yandex.ru/maps"),
            DocumentationInput.Url(22, "https://yandex.tj/maps"),
            DocumentationInput.Url(22, "https://yandex.tm/maps"),
            DocumentationInput.Url(22, "https://yandex.ua/maps"),
            DocumentationInput.Url(22, "https://yandex.uz/maps"),
        ),
    )
    override val shortUriPattern: Pattern =
        Pattern.compile("""(https?://)?yandex(\.[a-z]{2,3})?\.[a-z]{2,3}/maps/-/\S+""")
    override val shortUriMethod: ShortUriMethod = ShortUriMethod.HEAD

    @Suppress("SpellCheckingInspection")
    override val conversionUriPattern = conversionPattern {
        all {
            optional {
                first {
                    onUri { queryParams["whatshere%5Bzoom%5D"]?.let { it matcherIfMatches Z } } doReturn
                            { PositionMatch(it) }
                    onUri { queryParams["z"]?.let { it matcherIfMatches Z } } doReturn { PositionMatch(it) }
                }
            }
            first {
                onUri { queryParams["whatshere%5Bpoint%5D"]?.let { it matcherIfMatches "$LON,$LAT" } } doReturn
                        { PositionMatch(it) }
                onUri { queryParams["ll"]?.let { it matcherIfMatches "$LON,$LAT" } } doReturn { PositionMatch(it) }
                onUri { path matcherIfMatches """/maps/org/\d+([/?#].*|$)""" } doReturn { PositionMatch(it) }
            }
        }
    }

    override val conversionHtmlPattern = conversionPattern {
        onHtml { this matcherIfFind """data-coordinates="$LON,$LAT"""" } doReturn { PositionMatch(it) }
    }

    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_yandex_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_yandex_maps_loading_indicator_title
}
