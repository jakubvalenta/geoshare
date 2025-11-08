package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.PositionMatch
import page.ooooo.geoshare.lib.PositionMatch.Companion.LAT
import page.ooooo.geoshare.lib.PositionMatch.Companion.LON
import page.ooooo.geoshare.lib.PositionMatch.Companion.Z
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.conversionPattern
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.matches

object YandexMapsInput : Input.HasUri, Input.HasShortUri, Input.HasHtml {
    private val srs = Srs.WGS84

    override val uriPattern: Pattern = Pattern.compile("""(https?://)?yandex(\.[a-z]{2,3})?\.[a-z]{2,3}/\S+""")
    override val documentation = Input.Documentation(
        nameResId = R.string.converter_yandex_maps_name,
        inputs = listOf(
            Input.DocumentationInput.Url(20, "https://ya.ru/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.az/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.by/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.co.il/maps"),
            Input.DocumentationInput.Url(20, "https://yandex.com/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.com.am/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.com.ge/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.com.tr/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.ee/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.eu/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.fr/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.kg/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.kz/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.lt/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.lv/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.md/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.ru/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.tj/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.tm/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.ua/maps"),
            Input.DocumentationInput.Url(22, "https://yandex.uz/maps"),
        ),
    )
    override val shortUriPattern: Pattern =
        Pattern.compile("""(https?://)?yandex(\.[a-z]{2,3})?\.[a-z]{2,3}/maps/-/\S+""")
    override val shortUriMethod = Input.ShortUriMethod.HEAD

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
