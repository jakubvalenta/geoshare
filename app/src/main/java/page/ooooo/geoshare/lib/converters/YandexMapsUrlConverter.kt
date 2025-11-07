package page.ooooo.geoshare.lib.converters

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.PositionMatch
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.Srs
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.conversionPattern
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.matches

class YandexMapsUrlConverter() :
    UrlConverter.WithUriPattern,
    UrlConverter.WithShortUriPattern,
    UrlConverter.WithHtmlPattern {

    private val srs = Srs.WGS84

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
    override val shortUriMethod = ShortUriMethod.HEAD

    override val conversionUriPattern = conversionPattern<Uri, PositionMatch> {
        all {
            optional {
                first {
                    @Suppress("SpellCheckingInspection")
                    on { queryParams["whatshere%5Bzoom%5D"]?.let { it matches Z } } doReturn { PositionMatch(it, srs) }
                    on { queryParams["z"]?.let { it matches Z } } doReturn { PositionMatch(it, srs) }
                }
            }
            first {
                @Suppress("SpellCheckingInspection")
                on { queryParams["whatshere%5Bpoint%5D"]?.let { it matches "$LON,$LAT" } } doReturn {
                    PositionMatch(
                        it,
                        srs
                    )
                }
                on { queryParams["ll"]?.let { it matches "$LON,$LAT" } } doReturn { PositionMatch(it, srs) }
                on { path matches """/maps/org/\d+([/?#].*|$)""" } doReturn { PositionMatch(it, srs) }
            }
        }
    }

    override val conversionHtmlPattern = conversionPattern<String, PositionMatch> {
        on { this find """data-coordinates="$LON,$LAT"""" } doReturn { PositionMatch(it, srs) }
    }

    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_yandex_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_yandex_maps_loading_indicator_title
}
