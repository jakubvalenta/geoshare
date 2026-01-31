package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.collections.immutable.ImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.lib.point.asWGS84
import page.ooooo.geoshare.lib.point.buildPoints
import page.ooooo.geoshare.lib.point.toParseHtmlResult
import page.ooooo.geoshare.lib.point.toParseUriResult

object YandexMapsInput : Input.HasShortUri, Input.HasHtml {
    override val uriPattern = Regex("""(?:https?://)?yandex(?:\.[a-z]{2,3})?\.[a-z]{2,3}/\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.YANDEX_MAPS,
        nameResId = R.string.converter_yandex_maps_name,
        items = listOf(
            InputDocumentationItem.Url(20, "https://ya.ru/maps"),
            InputDocumentationItem.Url(22, "https://yandex.az/maps"),
            InputDocumentationItem.Url(22, "https://yandex.by/maps"),
            InputDocumentationItem.Url(22, "https://yandex.co.il/maps"),
            InputDocumentationItem.Url(20, "https://yandex.com/maps"),
            InputDocumentationItem.Url(22, "https://yandex.com.am/maps"),
            InputDocumentationItem.Url(22, "https://yandex.com.ge/maps"),
            InputDocumentationItem.Url(22, "https://yandex.com.tr/maps"),
            InputDocumentationItem.Url(22, "https://yandex.ee/maps"),
            InputDocumentationItem.Url(22, "https://yandex.eu/maps"),
            InputDocumentationItem.Url(22, "https://yandex.fr/maps"),
            InputDocumentationItem.Url(22, "https://yandex.kg/maps"),
            InputDocumentationItem.Url(22, "https://yandex.kz/maps"),
            InputDocumentationItem.Url(22, "https://yandex.lt/maps"),
            InputDocumentationItem.Url(22, "https://yandex.lv/maps"),
            InputDocumentationItem.Url(22, "https://yandex.md/maps"),
            InputDocumentationItem.Url(22, "https://yandex.ru/maps"),
            InputDocumentationItem.Url(22, "https://yandex.tj/maps"),
            InputDocumentationItem.Url(22, "https://yandex.tm/maps"),
            InputDocumentationItem.Url(22, "https://yandex.ua/maps"),
            InputDocumentationItem.Url(22, "https://yandex.uz/maps"),
        ),
    )
    override val shortUriPattern =
        Regex("""(?:https?://)?yandex(?:\.[a-z]{2,3})?\.[a-z]{2,3}/maps/-/\S+""")
    override val shortUriMethod = Input.ShortUriMethod.HEAD

    override suspend fun parseUri(uri: Uri): ParseUriResult? {
        var htmlUriString: String? = null
        return buildPoints {
            uri.run {
                @Suppress("SpellCheckingInspection")
                LON_LAT_PATTERN.matchEntire(queryParams["whatshere[point]"])?.toLonLatPoint()?.also { points.add(it) }
                    ?: LON_LAT_PATTERN.matchEntire(queryParams["ll"])?.toLonLatPoint()?.also { points.add(it) }

                @Suppress("SpellCheckingInspection")
                Z_PATTERN.matchEntire(queryParams["whatshere[zoom]"])?.doubleGroupOrNull()?.also { defaultZ = it }
                    ?: Z_PATTERN.matchEntire(queryParams["z"])?.doubleGroupOrNull()?.also { defaultZ = it }

                if (points.isEmpty() && Regex("""/maps/org/\d+(?:[/?#].*|$)""").matches(path)) {
                    htmlUriString = uri.toString()
                }
            }
        }
            .asWGS84()
            .toParseUriResult(htmlUriString)
    }

    override suspend fun parseHtml(
        channel: ByteReadChannel,
        pointsFromUri: ImmutableList<Point>,
        log: ILog,
    ): ParseHtmlResult? =
        buildPoints {
            defaultName = pointsFromUri.lastOrNull()?.name

            val pattern = Regex("""ll=$LON%2C$LAT""")
            while (true) {
                val line = channel.readUTF8Line() ?: break
                pattern.find(line)?.toLonLatPoint()?.also {
                    points.add(it)
                    break
                }
            }
        }
            .asWGS84()
            .toParseHtmlResult()

    @StringRes
    override val permissionTitleResId = R.string.converter_yandex_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_yandex_maps_loading_indicator_title
}
