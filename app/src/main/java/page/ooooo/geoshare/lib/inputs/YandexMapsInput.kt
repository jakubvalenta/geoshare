package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import kotlinx.io.Source
import kotlinx.io.readLine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ConversionPattern
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LAT
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LON
import page.ooooo.geoshare.lib.ConversionPattern.Companion.LON_LAT_PATTERN
import page.ooooo.geoshare.lib.ConversionPattern.Companion.Z_PATTERN
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.position.toLatLon
import page.ooooo.geoshare.lib.position.toZ

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

    override val conversionUriPattern = ConversionPattern.first<Uri, Position> {
        all {
            optional {
                first {
                    @Suppress("SpellCheckingInspection")
                    pattern { (Z_PATTERN match queryParams["whatshere%5Bzoom%5D"])?.toZ(srs) }
                    pattern { (Z_PATTERN match queryParams["z"])?.toZ(srs) }
                }
            }
            first {
                @Suppress("SpellCheckingInspection")
                pattern { (LON_LAT_PATTERN match queryParams["whatshere%5Bpoint%5D"])?.toLatLon(srs) }
                pattern { (LON_LAT_PATTERN match queryParams["ll"])?.toLatLon(srs) }
                pattern { ("""/maps/org/\d+([/?#].*|$)""" match path)?.let { Position(srs) } }
            }
        }
    }

    override val conversionHtmlPattern = ConversionPattern.first<Source, Position> {
        pattern {
            val pattern = Pattern.compile("""data-coordinates="$LON,$LAT"""")
            generateSequence { this.readLine() }
                .firstNotNullOfOrNull { line -> pattern find line }
                ?.toLatLon(srs)
        }
    }

    override val conversionHtmlRedirectPattern = null

    @StringRes
    override val permissionTitleResId = R.string.converter_yandex_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_yandex_maps_loading_indicator_title
}
