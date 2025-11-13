package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import com.google.re2j.Pattern
import kotlinx.io.Source
import kotlinx.io.readLine
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.find
import page.ooooo.geoshare.lib.extensions.match
import page.ooooo.geoshare.lib.position.*

object YandexMapsInput : Input.HasShortUri, Input.HasHtml {
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

    override fun parseUri(uri: Uri) = uri.run {
        PositionBuilder(srs).apply {
            @Suppress("SpellCheckingInspection")
            setPointFromMatcher { LON_LAT_PATTERN match queryParams["whatshere%5Bpoint%5D"] }
            setPointFromMatcher { LON_LAT_PATTERN match queryParams["ll"] }
            @Suppress("SpellCheckingInspection")
            setZoomFromMatcher { Z_PATTERN match queryParams["whatshere%5Bzoom%5D"] }
            setZoomFromMatcher { Z_PATTERN match queryParams["z"] }
            setUriString { if (("""/maps/org/\d+([/?#].*|$)""" match path) != null) uri.toString() else null }
        }
    }

    override fun parseHtml(source: Source) = source.run {
        PositionBuilder(srs).apply {
            val pattern = Pattern.compile("""data-coordinates="$LON,$LAT"""")
            for (line in generateSequence { source.readLine() }) {
                (pattern find line)?.toPoint(srs)?.let { point ->
                    points.add(point)
                    break
                }
            }
        }
    }

    @StringRes
    override val permissionTitleResId = R.string.converter_yandex_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_yandex_maps_loading_indicator_title
}
